/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.targethandlers.affectscope;

import org.l2j.gameserver.geoengine.GeoEngine;
import org.l2j.gameserver.handler.AffectObjectHandler;
import org.l2j.gameserver.handler.IAffectObjectHandler;
import org.l2j.gameserver.handler.IAffectScopeHandler;
import org.l2j.gameserver.model.WorldObject;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.skills.targets.AffectScope;
import org.l2j.gameserver.model.skills.targets.TargetType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Range affect scope implementation. Gathers objects in area of target origin (including origin itself).
 * @author Nik
 */
public class Range implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature activeChar, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int affectRange = skill.getAffectRange();
		final int affectLimit = skill.getAffectLimit();
		
		// Target checks.
		final TargetType targetType = skill.getTargetType();
		final AtomicInteger affected = new AtomicInteger(0);
		final Predicate<Creature> filter = c ->
		{
			if ((affectLimit > 0) && (affected.get() >= affectLimit))
			{
				return false;
			}
			if (c.isDead() && (targetType != TargetType.NPC_BODY) && (targetType != TargetType.PC_BODY))
			{
				return false;
			}
			if ((c == activeChar) && (target != activeChar)) // Range skills appear to not affect you unless you are the main target.
			{
				return false;
			}
			if ((c != target) && (affectObject != null) && !affectObject.checkAffectedObject(activeChar, c))
			{
				return false;
			}
			if (!GeoEngine.getInstance().canSeeTarget(target, c))
			{
				return false;
			}
			
			affected.incrementAndGet();
			return true;
		};
		
		// Check and add targets.
		if (targetType == TargetType.GROUND)
		{
			if (activeChar.isPlayable())
			{
				final Location worldPosition = activeChar.getActingPlayer().getCurrentSkillWorldPosition();
				if (worldPosition != null)
				{
					L2World.getInstance().forEachVisibleObjectInRange(activeChar, Creature.class, (int) (affectRange + activeChar.calculateDistance2D(worldPosition)), c ->
					{
						if (!c.isInsideRadius3D(worldPosition, affectRange))
						{
							return;
						}
						if (filter.test(c))
						{
							action.accept(c);
						}
					});
				}
			}
		}
		else
		{
			// Add object of origin since its skipped in the forEachVisibleObjectInRange method.
			if (target.isCharacter() && filter.test((Creature) target))
			{
				action.accept(target);
			}
			
			L2World.getInstance().forEachVisibleObjectInRange(target, Creature.class, affectRange, c ->
			{
				if (filter.test(c))
				{
					action.accept(c);
				}
			});
		}
	}
	
	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.RANGE;
	}
}
