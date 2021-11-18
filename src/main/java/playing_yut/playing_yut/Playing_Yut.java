package playing_yut.playing_yut;

import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public final class Playing_Yut extends JavaPlugin implements Listener {

    final Material YUT_ITEM = Material.BLAZE_ROD;

    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("윷놀이 플러그인 로드됨");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("윷놀이 플러그인 언로드됨");
    }


    public void throwYut(Player throwPlayer){

        double startY = throwPlayer.getLocation().getY();

        MyUtility.playSoundForAllPlayer(Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 0.5f);

        ItemStack throwStack = new ItemStack(YUT_ITEM, 1);

        Location pLoc = throwPlayer.getEyeLocation();

        Item thrownItem = throwPlayer.getWorld().dropItem(pLoc, throwStack);
        thrownItem.setVelocity(pLoc.getDirection().multiply(1.25f));
        thrownItem.setPickupDelay(40); //2초 후에 주울 수 있음

        SpreadYut spreadYut = new SpreadYut();
        spreadYut.startLoc = null;
        spreadYut.yut_item = thrownItem;
        spreadYut.setStartLocSchedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()->{

            Location nowLoc = thrownItem.getLocation();
            if(nowLoc.getY() > startY + 3){ //던진 위치에서 최소 3 이상의 높이라면
                if(spreadYut.startLoc == null || spreadYut.startLoc.getY() < nowLoc.getY()){
                    spreadYut.startLoc = nowLoc;
                } else {
                    Bukkit.getScheduler().cancelTask(spreadYut.setStartLocSchedulerId);
                    spreadYut.spread();
                }
            }
            if(spreadYut.setStartLocSchedulerTimer++ > 40){
                if(spreadYut.startLoc == null){
                    MyUtility.sendMessageForAllPlayer(YutMessage.MORE_HIGHER_YUT);
                    Bukkit.getScheduler().cancelTask(spreadYut.setStartLocSchedulerId);
                }
            }

        }, 0, 1);

    }

    public Player findPlayer(String playerName){
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if(onlinePlayer.getName().equalsIgnoreCase(playerName)){
                return onlinePlayer;
            }
        }
        return null;
    }

    public void giveYut(String playerName){

        Player targetPlayer = findPlayer(playerName);

        if(targetPlayer == null)
            return;

        ItemStack yut = new ItemStack(YUT_ITEM, 1);
        ItemMeta meta = yut.getItemMeta();
        meta.setDisplayName("§6윷");
        yut.setItemMeta(meta);

        targetPlayer.getInventory().addItem(yut);

    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String[] cmd = e.getMessage().split(" ");

        if (cmd[0].equalsIgnoreCase("/윷") || cmd[0].equalsIgnoreCase("/yut")) {
            e.setCancelled(true);

            if(cmd.length >= 2){
                if (p.isOp()) {
                    giveYut(cmd[1]);
                }
            } else {
                p.sendMessage(YutMessage.YUT_COMMAND);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractItem(PlayerInteractEvent e){
        ItemStack interactedItem = e.getItem();

        if(interactedItem == null) return;

        if(interactedItem.getType() == YUT_ITEM){
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                MyUtility.removeItem(e.getPlayer(), YUT_ITEM, 1);
                throwYut(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {

        ArrayList<String> yutList = new ArrayList<String>();
        int sucCnt = 0;

        if(e.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) e.getEntity();

            if(fallingBlock.getCustomName() != null &&
                    (fallingBlock.getCustomName().equalsIgnoreCase(SpreadYut.defaultYut)
                    || fallingBlock.getCustomName().equalsIgnoreCase(SpreadYut.backTypeYut))) {

                Location l = fallingBlock.getLocation();

                MyUtility.playSoundForAllPlayer(Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);

                int rd = MyUtility.getRandom(0, 1);

                if(rd == 0){
                    yutList.add("실패");
                    l.getBlock().setType(SpreadYut.falseYut);
                } else if(rd == 1){
                    if(fallingBlock.getCustomName().equalsIgnoreCase(SpreadYut.backTypeYut)){
                        yutList.add("백도");
                        l.getBlock().setType(SpreadYut.backYut);
                    } else {
                        yutList.add("성공");
                        l.getBlock().setType(SpreadYut.trueYut);
                    }
                }

                e.setCancelled(true);
                fallingBlock.remove();
            }
        }

//        for(String result : yutList){
//            if(result.equalsIgnoreCase("성공") || result.equalsIgnoreCase("백도")){
//                sucCnt += 1;
//            }
//            if(sucCnt == 1){
//                if(yutList.contains("백도")){
//                    MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b백도!");
//                } else {
//                    MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b도!");
//                }
//
//            } else if(sucCnt == 2){
//                MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b개!");
//            } else if(sucCnt == 3){
//                MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b걸!");
//            } else if(sucCnt == 4){
//                MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b윷!");
//            } else if(sucCnt == 0){
//                MyUtility.sendMessageForAllPlayer("§f[§6 한글 윷놀이 §f] §b모!");
//            }
//        }
    }
}
