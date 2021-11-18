package playing_yut.playing_yut;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class SpreadYut {

    final public static Material fallingYut = Material.YELLOW_WOOL;
    final public static Material trueYut = Material.BLUE_WOOL;
    final public static Material backYut = Material.CYAN_WOOL;
    final public static Material falseYut = Material.RED_WOOL;

    final public static String backTypeYut = "윷_백도";
    final public static String defaultYut = "윷";

    public Location startLoc;
    public Item yut_item;
    public int setStartLocSchedulerId;
    public int setStartLocSchedulerTimer;

    private boolean isBackYutCreated = false;

    public void spread(){

        Location spreadLoc = yut_item.getLocation().clone();

        yut_item.remove();
        spreadEffect(spreadLoc);

        spreadYutBlock(spreadLoc);

    }

    public void spreadEffect(Location l){

        for(int i = 0; i < 4; i ++){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Playing_Yut.plugin, ()->{
                Location tmpL = l.clone();

                tmpL.add(MyUtility.getRandom(-3, 3), MyUtility.getRandom(-3, 3), MyUtility.getRandom(-3, 3));
                Firework fw = (Firework) tmpL.getWorld().spawnEntity(tmpL, EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                fwm.setPower(1);
                fwm.addEffect(FireworkEffect.builder().withColor(MyUtility.getRandomColor()).flicker(true).build());

                fw.setFireworkMeta(fwm);
                fw.detonate();

//                Firework fw2 = (Firework) tmpL.getWorld().spawnEntity(tmpL, EntityType.FIREWORK);
//                fw2.setFireworkMeta(fwm);
            }, i * 8);
        }

    }

    public void spreadYutBlock(Location l){

        isBackYutCreated = false;

        for(int i = 0; i < 4; i ++){
            final int index = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Playing_Yut.plugin, ()->{
                FallingBlock fb = l.getWorld().spawnFallingBlock(l, fallingYut, (byte)0);

                if(!isBackYutCreated){
                    if(MyUtility.getRandom(0, 1) == 1 || index == 3){ //마지막 윷인데도 아직 백도 없으면
                        fb.setCustomName(backTypeYut);
                        isBackYutCreated = true;
                    } else {
                        fb.setCustomName(defaultYut);
                    }
                } else {
                    fb.setCustomName(defaultYut);
                }

                Vector randomVector = Vector.getRandom();
                randomVector.setY(0);
                randomVector.setX(randomVector.getX() - 0.5f);
                randomVector.setZ(randomVector.getZ() - 0.5f); // Now it does //
                fb.setVelocity(randomVector);
            }, i * 8);

        }
    }

}
