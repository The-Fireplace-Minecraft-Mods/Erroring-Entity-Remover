package dev.the_fireplace.eer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin {
    private static final Logger LOGGER = LogManager.getLogger("eer");

    /**
     * @author The_Fireplace
     * @reason Avoid letting erroring entities crash the game if possible
     */
    @Overwrite
    public void tickEntity(Consumer<Entity> tickConsumer, Entity entity) {
        try {
            tickConsumer.accept(entity);
        } catch (Throwable throwable) {
            try {
                LOGGER.warn("Removing erroring entity of type {} at {} :", Registry.ENTITY_TYPE.getId(entity.getType()).toString(), entity.getPos().toString());
                LOGGER.warn(entity.writeNbt(new NbtCompound()).toString());
                entity.remove(Entity.RemovalReason.DISCARDED);
                LOGGER.error("Erroring Entity Stacktrace:", throwable);
            } catch(Exception e) {
                CrashReport crashReport = CrashReport.create(throwable, "Ticking entity");
                CrashReportSection crashReportSection = crashReport.addElement("Entity being ticked");
                entity.populateCrashReport(crashReportSection);
                throw new CrashException(crashReport);
            }
        }
    }
}
