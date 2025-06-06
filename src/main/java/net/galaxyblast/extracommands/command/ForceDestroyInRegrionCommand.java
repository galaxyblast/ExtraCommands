package net.galaxyblast.extracommands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.galaxyblast.extracommands.ExtraCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;
import java.util.List;

public class ForceDestroyInRegrionCommand
{

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        dispatcher.register(Commands.literal("forcedestroyinregion").requires(player -> player.hasPermission(2))
                                    .then(Commands.argument("blockID", BlockStateArgument.block(context))
                                    .then(Commands.argument("startBlockPos", BlockPosArgument.blockPos())
                                                  .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BlockPosArgument.blockPos().getExamples(), builder))
                                    .then(Commands.argument("endBlockPos", BlockPosArgument.blockPos())
                                                  .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BlockPosArgument.blockPos().getExamples(), builder))
                                                  .executes(cmd -> forceDestroyInRegion(cmd.getSource(),
                                                                                        BlockStateArgument.getBlock(cmd, "blockID"),
                                                                                        BlockPosArgument.getBlockPos(cmd, "startBlockPos"),
                                                                                        BlockPosArgument.getBlockPos(cmd, "endBlockPos")))
                                    ))));
    }

    public static int forceDestroyInRegion(CommandSourceStack source, BlockInput block, BlockPos startPos, BlockPos endPos)
    {
        ServerLevel level = source.getLevel();

        List<ChunkPos> chunks = ChunkPos.rangeClosed(new ChunkPos(startPos), new ChunkPos(endPos)).toList();

        for(ChunkPos chunkPos : chunks)
        {
            if(!level.hasChunk(chunkPos.x, chunkPos.z))
            {
                level.getChunkSource().addRegionTicket(ForceDestroyCommand.LOAD_REQUEST, chunkPos, 3, chunkPos);
            }
        }

        int count = 0;

        int startX = Math.min(startPos.getX(), endPos.getX());
        int startY = Math.min(startPos.getY(), endPos.getY());
        int startZ = Math.min(startPos.getZ(), endPos.getZ());
        int endX = Math.max(startPos.getX(), endPos.getX());
        int endY = Math.max(startPos.getY(), endPos.getY());
        int endZ = Math.max(startPos.getZ(), endPos.getZ());

        for(int x = startX; x < endX; x++)
        {
            for(int y = startY; y < endY; y++)
            {
                for(int z = startZ; z < endZ; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    if(level.getBlockState(pos).getBlock().equals(block.getState().getBlock()))
                    {
                        level.destroyBlock(pos, false);
                        count++;
                    }
                }
            }
        }

        //level.destroyBlock(pos, false);

        ExtraCommands.LOGGER.info("Destroyed " + count + " instances of " + block.getState().getBlock().toString());

        return count;
    }
}
