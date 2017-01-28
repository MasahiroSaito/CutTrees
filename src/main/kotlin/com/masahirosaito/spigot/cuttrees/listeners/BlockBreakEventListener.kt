package com.masahirosaito.spigot.cuttrees.listeners

import com.masahirosaito.spigot.cuttrees.CutTrees
import com.masahirosaito.spigot.cuttrees.utils.*
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class BlockBreakEventListener(val plugin: CutTrees) : Listener {
    val configs = plugin.configs

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!isValid(event)) return

        var blocks = calcBreakBLocks(event.block)
        val player = event.player
        val tool = player.itemInMainHand()
        val durability = tool.getRemainingDurability()
        var damage = blocks.size

        if (isNotReduceToolDurability(player, tool)) {
            breakBlocks(blocks, tool)
            sendBlockBreakMessage(player, durability, 0, blocks.size)
            return
        }

        if (tool.containsEnchantment(Enchantment.DURABILITY)) {
            for (i in 1..blocks.size) {
                damage -= if (isReduce(tool)) 0 else 1
            }
        }

        if (tool.isBreak(damage)) {
            blocks = blocks.take(tool.getRemainingDurability())
        }

        breakBlocks(blocks, tool)

        if (tool.isBreak(damage)) {
            player.onBreakItemInMainHand()
        } else {
            tool.damage(damage)
        }

        sendBlockBreakMessage(player, durability, damage, blocks.size)
    }

    private fun isValid(event: BlockBreakEvent): Boolean = when {
        !configs.isValid(event.block) -> false
        !configs.isValid(event.player.itemInMainHand()) -> false
        else -> true
    }

    private fun calcBreakBLocks(block: Block): List<Block> {
        val unCheckedBlocks = mutableSetOf(block)
        val checkedBlocks = mutableSetOf<Block>()

        while (unCheckedBlocks.isNotEmpty() && configs.isNotMax(checkedBlocks)) {
            val b = unCheckedBlocks.first().apply {
                unCheckedBlocks.remove(this)
                checkedBlocks.add(this)
            }
            unCheckedBlocks.addAll(b.getRelatives(configs.rangeBreakBlock)
                    .filter(Block::isTree)
                    .filter { b.asTree().sameSpecies(block.asTree()) }
                    .filterNot { checkedBlocks.contains(it) }
            )
        }
        return checkedBlocks.toList()
    }

    private fun breakBlocks(blocks: Collection<Block>, tool: ItemStack) =
            blocks.forEach { it.breakNaturally(tool) }

    private fun isReduce(tool: ItemStack): Boolean =
            Random().nextInt(tool.getEnchantmentLevel(Enchantment.DURABILITY)) == 0

    private fun sendBlockBreakMessage(player: Player, durability: Int, damage: Int, blocksNum: Int) {
        if (configs.onBlockBreakMessage) {
            val newDurability = if (durability - damage >= 0) durability - damage else 0
            player.sendMessage("耐久値: $durability -> $newDurability, ブロック数: $blocksNum")
        }
    }

    private fun isNotReduceToolDurability(player: Player, tool: ItemStack): Boolean = when {
        (player.isCreativeMode() && !configs.onCreativeDurabilityReduce) -> true
        (tool.itemMeta.spigot().isUnbreakable) -> true
        else -> false
    }

    fun register() = plugin.server.pluginManager.registerEvents(this, plugin)
}