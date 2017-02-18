package com.masahirosaito.spigot.cuttrees.trees

import com.masahirosaito.spigot.cuttrees.materials.DurabilityMaterial
import com.masahirosaito.spigot.cuttrees.utils.*
import org.bukkit.Material
import org.bukkit.TreeSpecies
import org.bukkit.block.Block

class OakTree(block: Block) : BaseTree(block) {

    override fun growingOn() = arrayOf(
            DurabilityMaterial(Material.DIRT, 0),
            DurabilityMaterial(Material.DIRT, 1),
            DurabilityMaterial(Material.GRASS, 0)
    )

    override fun material(): Material = Material.LOG

    override fun heightRange() = 4 to height()

    override fun leavesRange(): Int = 3

    override fun branchRange(): Int = 4

    override fun relativeRange(): Int = 1

    override fun isInValid(blocks: MutableSet<Block>): Boolean = false

    override fun isSame(block: Block): Boolean {
        return if (block.isTree()) block.asTree().species == TreeSpecies.GENERIC else false
    }

    override fun isSameLeaves(block: Block): Boolean {
        return if (block.isLeaves()) block.asLeaves().species == TreeSpecies.GENERIC else false
    }

    override fun bottomsRange() = 1 to bottoms.size
}