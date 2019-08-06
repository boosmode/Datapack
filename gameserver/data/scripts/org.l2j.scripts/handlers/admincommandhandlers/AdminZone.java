package handlers.admincommandhandlers;

import org.l2j.gameserver.cache.HtmCache;
import org.l2j.gameserver.handler.IAdminCommandHandler;
import org.l2j.gameserver.world.MapRegionManager;
import org.l2j.gameserver.world.zone.ZoneManager;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.TeleportWhereType;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.world.zone.Zone;
import org.l2j.gameserver.world.zone.ZoneId;
import org.l2j.gameserver.world.zone.type.SpawnTerritory;
import org.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2j.gameserver.util.BuilderUtil;

import java.util.StringTokenizer;

/**
 * Small typo fix by Zoey76 24/02/2011
 */
public class AdminZone implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_zone_check",
                    "admin_zone_visual",
                    "admin_zone_visual_clear"
            };

    @Override
    public boolean useAdminCommand(String command, Player player)
    {
        if (player == null)
        {
            return false;
        }

        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken(); // Get actual command

        if (actualCommand.equalsIgnoreCase("admin_zone_check"))
        {
            showHtml(player);
            BuilderUtil.sendSysMessage(player, "MapRegion: x:" + MapRegionManager.getInstance().getMapRegionX(player.getX()) + " y:" + MapRegionManager.getInstance().getMapRegionY(player.getY()) + " (" + MapRegionManager.getInstance().getMapRegionLocId(player) + ")");
            getGeoRegionXY(player);
            BuilderUtil.sendSysMessage(player, "Closest Town: " + MapRegionManager.getInstance().getClosestTownName(player));

            // Prevent exit instance variable deletion.
            if (!player.isInInstance())
            {
                Location loc;

                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
                BuilderUtil.sendSysMessage(player, "TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CLANHALL);
                BuilderUtil.sendSysMessage(player, "TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.SIEGEFLAG);
                BuilderUtil.sendSysMessage(player, "TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
                BuilderUtil.sendSysMessage(player, "TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
            }
        }
        else if (actualCommand.equalsIgnoreCase("admin_zone_visual"))
        {
            final String next = st.nextToken();
            if (next.equalsIgnoreCase("all"))
            {
                for (Zone zone : ZoneManager.getInstance().getZones(player))
                {
                    zone.visualizeZone(player.getZ());
                }
                for (SpawnTerritory territory : ZoneManager.getInstance().getSpawnTerritories(player))
                {
                    territory.visualizeZone(player.getZ());
                }
                showHtml(player);
            }
            else
            {
                final int zoneId = Integer.parseInt(next);
                ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(player.getZ());
            }
        }
        else if (actualCommand.equalsIgnoreCase("admin_zone_visual_clear"))
        {
            ZoneManager.getInstance().clearDebugItems();
            showHtml(player);
        }
        return true;
    }

    private static void showHtml(Player activeChar)
    {
        final String htmContent = HtmCache.getInstance().getHtm(activeChar, "data/html/admin/zone.htm");
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(0, 1);
        adminReply.setHtml(htmContent);
        adminReply.replace("%PEACE%", activeChar.isInsideZone(ZoneId.PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%PVP%", activeChar.isInsideZone(ZoneId.PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%SIEGE%", activeChar.isInsideZone(ZoneId.SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%CASTLE%", activeChar.isInsideZone(ZoneId.CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%FORT%", activeChar.isInsideZone(ZoneId.FORT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%HQ%", activeChar.isInsideZone(ZoneId.HQ) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%CLANHALL%", activeChar.isInsideZone(ZoneId.CLAN_HALL) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%LAND%", activeChar.isInsideZone(ZoneId.LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%NOLAND%", activeChar.isInsideZone(ZoneId.NO_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%NOSUMMON%", activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%WATER%", activeChar.isInsideZone(ZoneId.WATER) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%FISHING%", activeChar.isInsideZone(ZoneId.FISHING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%SWAMP%", activeChar.isInsideZone(ZoneId.SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%DANGER%", activeChar.isInsideZone(ZoneId.DANGER_AREA) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%NOSTORE%", activeChar.isInsideZone(ZoneId.NO_STORE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%SCRIPT%", activeChar.isInsideZone(ZoneId.SCRIPT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
        adminReply.replace("%TAX%", (activeChar.isInsideZone(ZoneId.TAX) ? "<font color=\"LEVEL\">YES</font>" : "NO"));

        final StringBuilder zones = new StringBuilder(100);
        for (Zone zone : ZoneManager.getInstance().getZones(activeChar))
        {
            if (zone.getName() != null)
            {
                zones.append(zone.getName());
                if (zone.getId() < 300000)
                {
                    zones.append(" (");
                    zones.append(zone.getId());
                    zones.append(")");
                }
                zones.append("<br1>");
            }
            else
            {
                zones.append(zone.getId());
            }
            zones.append(" ");
        }
        for (SpawnTerritory territory : ZoneManager.getInstance().getSpawnTerritories(activeChar))
        {
            zones.append(territory.getName());
            zones.append("<br1>");
        }
        adminReply.replace("%ZLIST%", zones.toString());
        activeChar.sendPacket(adminReply);
    }

    private static void getGeoRegionXY(Player activeChar)
    {
        final int worldX = activeChar.getX();
        final int worldY = activeChar.getY();
        final int geoX = (((worldX - -327680) >> 4) >> 11) + 10;
        final int geoY = (((worldY - -262144) >> 4) >> 11) + 10;
        BuilderUtil.sendSysMessage(activeChar, "GeoRegion: " + geoX + "_" + geoY + "");
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}
