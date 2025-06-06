package net.galaxyblast.extracommands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.galaxyblast.extracommands.ExtraCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;

public class ForceDestroyCommand
{
    public static final TicketType<ChunkPos> LOAD_REQUEST = TicketType.create("force_destroy", Comparator.comparingLong(ChunkPos::toLong), 1200);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        dispatcher.register(Commands.literal("forcedestroy").requires(player -> player.hasPermission(2))
                                    .then(Commands.argument("blockPos", BlockPosArgument.blockPos())
                                                  .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BlockPosArgument.blockPos().getExamples(), builder))
                                                  .executes(cmd -> forceDestroy(cmd.getSource(), BlockPosArgument.getBlockPos(cmd, "blockPos")))
                                    ));
    }

    public static int forceDestroy(CommandSourceStack source, BlockPos pos)
    {
        ServerLevel level = source.getLevel();

        ChunkPos chunkPos = new ChunkPos(pos);
        if(!level.hasChunk(chunkPos.x, chunkPos.z))
        {
            level.getChunkSource().addRegionTicket(LOAD_REQUEST, chunkPos, 3, chunkPos);
        }

        Block block = level.getBlockState(pos).getBlock();
        level.destroyBlock(pos, false);
        ExtraCommands.LOGGER.info("Destroyed " + block.toString() + " at " + pos.toString());

        return 1;
    }
}
