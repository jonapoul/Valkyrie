package io.github.composegears.valkyrie.generator.core

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlin.test.assertEquals

class IconPackTest {

    @Test
    fun `toString should handle empty IconPack`() {
        val iconPack = IconPack(name = "")
        val expected = ""

        assertEquals(expected, iconPack.toString())
    }

    @Test
    fun `toString should return formatted tree structure`() {
        val iconPack = IconPack(
            name = "Root",
            nested = listOf(
                IconPack(
                    name = "Child1",
                    nested = listOf(
                        IconPack(name = "Grandchild1"),
                        IconPack(name = "Grandchild2"),
                    ),
                ),
                IconPack(name = "Child2"),
            ),
        )

        val expected = """

            Root:
            ├── Child1
            │	├── Grandchild1
            │	└── Grandchild2
            └── Child2

        """.trimIndent()

        assertEquals(expected, iconPack.toString())
    }

    @Test
    fun `toString should handle deeply nested IconPack`() {
        val iconPack = IconPack(
            name = "Root",
            nested = listOf(
                IconPack(
                    name = "Child1",
                    nested = listOf(
                        IconPack(
                            name = "Grandchild1",
                            nested = listOf(
                                IconPack(
                                    name = "GreatGrandchild1",
                                    nested = listOf(
                                        IconPack(name = "GreatGreatGrandchild1"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                IconPack(
                    name = "Child2",
                    nested = listOf(
                        IconPack(name = "Grandchild2"),
                    ),
                ),
            ),
        )

        val expected = """

            Root:
            ├── Child1
            │	└── Grandchild1
            │		└── GreatGrandchild1
            │			└── GreatGreatGrandchild1
            └── Child2
            	└── Grandchild2

        """.trimIndent()

        assertEquals(expected, iconPack.toString())
    }

    @Test
    fun `fromString parsing empty string`() {
        val input = ""
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("")
        assertThat(result.nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo(input)
    }

    @Test
    fun `fromString parsing with multiple roots`() {
        val input = "RootA.Child1,RootB.Child2"

        assertFailure {
            IconPack.fromString(input)
        }.isInstanceOf(IllegalStateException::class)
    }

    @Test
    fun `fromString parsing single segment`() {
        val input = "Root"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("Root")
        assertThat(result.nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo(input)
    }

    @Test
    fun `fromString parsing L1 hierarchy`() {
        val input = "Root.Child1,Root.Child2"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("Root")
        assertThat(result.nested).hasSize(2)
        assertThat(result.nested[0].name).isEqualTo("Child1")
        assertThat(result.nested[0].nested).isEmpty()
        assertThat(result.nested[1].name).isEqualTo("Child2")
        assertThat(result.nested[1].nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo(input)
    }

    @Test
    fun `fromString parsing deeper hierarchy`() {
        val input = "Root.Child1,Root.Child1.GrandChild1,Root.Child2"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("Root")
        assertThat(result.nested).hasSize(2)

        val child1 = result.nested.find { it.name == "Child1" }!!
        val child2 = result.nested.find { it.name == "Child2" }!!

        assertThat(child1.nested).hasSize(1)
        assertThat(child1.nested[0].name).isEqualTo("GrandChild1")
        assertThat(child1.nested[0].nested).isEmpty()

        assertThat(child2.nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo("Root.Child1.GrandChild1,Root.Child2")
    }

    @Test
    fun `fromString parsing multiple levels deep`() {
        val input = "Root.Child1.GrandChild1.GreatGrandChild1,Root.Child2,Root.Child1"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("Root")
        assertThat(result.nested).hasSize(2)

        val child1 = result.nested.find { it.name == "Child1" }!!
        val grandChild1 = child1.nested[0]
        val greatGrandChild1 = grandChild1.nested[0]

        assertThat(grandChild1.name).isEqualTo("GrandChild1")
        assertThat(greatGrandChild1.name).isEqualTo("GreatGrandChild1")
        assertThat(greatGrandChild1.nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo("Root.Child1.GrandChild1.GreatGrandChild1,Root.Child2")
    }

    @Test
    fun `fromString parsing with complex nested structure`() {
        val input = "Root.A.X,Root.A.Y,Root.B.Z,Root.B.W.V"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("Root")
        assertThat(result.nested).hasSize(2)

        val childA = result.nested.find { it.name == "A" }!!
        val childB = result.nested.find { it.name == "B" }!!

        assertThat(childA.nested).hasSize(2)
        val namesInA = childA.nested.map { it.name }
        assertThat(namesInA).containsAtLeast("X", "Y")

        assertThat(childB.nested).hasSize(2)
        val childZ = childB.nested.find { it.name == "Z" }!!
        val childW = childB.nested.find { it.name == "W" }!!

        assertThat(childZ.nested).isEmpty()
        assertThat(childW.nested).hasSize(1)

        val childV = childW.nested[0]
        assertThat(childV.name).isEqualTo("V")
        assertThat(childV.nested).isEmpty()

        assertThat(IconPack.toRawString(result)).isEqualTo(input)
    }

    @Test
    fun `fromString parsing with shared paths`() {
        val input = "AAA.BB,AAA.CC,AAA.BB.FF,AAA.BB.FF.CC.AAA,AAA.CC.BB"
        val result = IconPack.fromString(input)

        assertThat(result.name).isEqualTo("AAA")
        assertThat(result.nested).hasSize(2)

        val childBB = result.nested.find { it.name == "BB" }!!
        val childCC = result.nested.find { it.name == "CC" }!!

        assertThat(childBB.nested).hasSize(1)
        assertThat(childBB.nested[0].name).isEqualTo("FF")

        val childFF = childBB.nested[0]
        assertThat(childFF.nested).hasSize(1)
        assertThat(childFF.nested[0].name).isEqualTo("CC")

        val repeatedChildCC = childFF.nested[0]
        assertThat(repeatedChildCC.nested).hasSize(1)
        assertThat(repeatedChildCC.nested[0].name).isEqualTo("AAA")

        assertThat(childCC.nested).hasSize(1)
        assertThat(childCC.nested[0].name).isEqualTo("BB")

        assertThat(IconPack.toRawString(result)).isEqualTo("AAA.BB.FF.CC.AAA,AAA.CC.BB")
    }
}
