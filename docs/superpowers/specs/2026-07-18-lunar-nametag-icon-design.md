# Lunar Nametag Icon Design

## Goal

Show the Lunar logo immediately to the left of the in-world nametag for every player present in `PeerRegistry`. Non-Lunar player nametags remain unchanged.

## Design

Add a bitmap font containing the existing Lunar logo and a small spacer glyph. A client mixin on the player renderer checks the rendered player's UUID during render-state extraction. When peer data exists and the nametag is visible, it prepends the logo and spacer to the existing nametag component, tinting the logo with that peer's RGB logo color.

Using an inline glyph leaves Minecraft responsible for centering, scaling, distance visibility, crouching behavior, background rendering, and existing nametag formatting. The tab-list renderer remains unchanged.

## Behavior

- Lunar player: tinted Lunar logo, small gap, existing nametag.
- Non-Lunar player: existing nametag only.
- Hidden nametag: no icon is added.
- Score text and other labels above the player remain unchanged.

## Verification

Add the smallest automated check practical for the component-building behavior, then run the Gradle test and build tasks. In a development client, verify one detected Lunar player and one ordinary player at normal and crouching distances.
