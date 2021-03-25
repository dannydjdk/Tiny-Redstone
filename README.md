Tiny Redstone is a forge mod for Minecraft that adds many tiny redstone pieces that you can put together on redstone panels to form tiny redstone circuits.
These circuits can be copied onto blueprints and shared inside and outside your Minecraft world.

These panels can be picked up, moved, rotated, and linked with other panels. You can even dye them to match your build!

Each panel can hold up to 64 components in an 8x8 grid. You can choose which sides each piece of Tiny Redstone Dust connects and make your circuits super compact.

## How to play

1. Craft a Redstone Panel and some tiny redstone components.
2. Right click the panel with the components to place them.
3. Left click with a component or Redstone Wrench in your hand to remove them.
3. Right click with a Redstone Wrench to rotate the panel.
4. Pick up the panel with all it's components and circuitry intact by shift-right clicking it with Redstone Wrench, or
simply use a pick or your bare hands.

### Tiny Redstone Dust ![Tiny Redstone](https://media.forgecdn.net/attachments/345/698/tiny_redstone.png "Tiny Redstone")

Tiny redstone behaves much like regular redstone. It picks up signals from strong redstone sources. Signals passed from one tiny redstone to another are decreased by 1. Unlike regular redstone, you can right click to toggle each edge of the redstone on or off. If you click directly in the middle, it will toggle all 4 directions on or off.

### Tiny Repeater

Tiny repeaters are similar to their bigger cousins. They will pick up any redstone signal received at the back and output a full (15) signal to the front with a delay
between 1 and 4 ticks set by right clicking.

### Tiny Super Repeater

Similar to Tiny Repeaters, except that when you right click, a GUI will appear allowing you to increase or decrease the delay up to 100 redstone ticks (10 seconds).

### Tiny Comparator

Tiny comparators work exactly like regular comparators. They will pick up weak signals from adjacent blocks and inventory comparator outputs when placed on the edge of a panel. You can also right click them to toggle subtract mode, in which case, just like vanilla comparators, the output will be subtracted by the inputs coming in from the sides.

### Tiny Redstone Torch ![Tiny Redstone Torch](https://media.forgecdn.net/attachments/345/700/tiny_redstone_torch.png "Tiny Redstone Torch")

Similar to regular redstone torches, but cuter. Tiny Redstone Torches can act as redstone power sources and as NOT gates. When placing a Tiny Redstone Torch, it will be point away from you. It will output a full redstone signal on 3 sides, and will switch off when receiving a redstone signal from the bottom side. It is not necessary to place the torch against a block to create a NOT gate.
These torches will also output a small amount of light when lit, similar to Tiny Redstone Lamps.

### Tiny Redstone Block ![Tiny Redstone Block](https://media.forgecdn.net/attachments/345/702/tiny_redstone_block.png "Tiny Redstone Block")

Like their huge counterpart, Tiny Redstone Blocks output full redstone power in all 4 directions and can be pushed by tiny pistons.

### Tiny Piston & Tiny Sticky Piston

Work almost exactly like their monstrous counterpart. They can push up to 12 tiny blocks.
However, they will not extend if a non-pushable block is in the way.
So, you don't have to worry about tiny redstone "popping" off the panel.
It will push tiny blocks onto adjacent Redstone Panels but will not push off the panel
if no adjacent Redstone Panel exists.

### Tiny Solid Block

Colorful little wool-like blocks. They're not just pretty. They are very useful.
They behave like solid blocks with vanilla redstone. They will carry a weak redstone
signal when powered by redstone, and a strong signal when powered by a repeater or comparator.
*And*, since they can be pushed by tiny pistons, you can build edge detectors and other powerful circuits!

### Tiny Glass Block

Colorful little glass-like blocks. They behave like transparent blocks with vanilla redstone in that they
do not carry a redstone signal, and they can be pushed by tiny pistons.

### Tiny Button
Just like vanilla buttons, but smaller. They output redstone on all 4 sides when activated.
Wood buttons output for 15 redstone ticks and stone for 10 redstone ticks.

### Tiny Lever
It's a lever, but tiny!

### Tiny Redstone Lamp
Like their bigger counterparts, they will light up when given a redstone signal.
The light they output is very dim. 15 lit lamps will output the max light of 15, so you can control
exactly how much light you want your panel to output.

### Tiny Observer
Work almost exactly like their big brother.
Outputs a pulse to the back whenever a change is detected in the cell or block in front of it.
When facing blocks out in the world, it behaves exactly like vanilla observers. When facing cells on the tile,
it will detect movement, placement, removal and redstone changes.
It currently does not detect color changes or tiny repeater delay changes. You probably didn't want it to anyway. ;)

*Note:* You can not break a Redstone Panel while you are holding a Redstone Wrench or a tiny component.
This is to help prevent accidental breakage while removing tiny components from the panel, especially in creative mode.
Sneak right click with the Redstone Wrench to instantly pick up the Redstone Panel, or you can always use a pickaxe.

## Copying and Sharing Circuits
### Saving a circuit
Craft a Blueprint and right click the empty blueprint on your assembled Redstone Panel.
The circuit on that panel is now saved to that blueprint.
To clear the blueprint, place it in a crafting grid.

### Copying a saved circuit
Hover over the blueprint to see the components required for your circuit.
Make sure you have all those components in your inventory (unless you are in Creative Mode).
Right click on an empty Redstone Panel.
The required components will be taken from your inventory and placed on the panel to form the circuit.

### Exporting
After saving a circuit to a blueprint, right click in the air. A GUI will appear.
Click the Export button and save the *.json file wherever you want it.

### Importing
Right click in the air with an empty blueprint. A GUI will appear.
Click the Import button. Find the *.json file with the saved circuit and open it.
The circuit will now be saved to your blueprint.

## Future Plans

- Tiny Levers.
- Panel covers to hide circuitry and reduce lag potential.
- One Probe support, so you can see the redstone signal of each tiny component.
- Possible add-ons - gates, clocks, redstone math (possibly as an add-on mod since I want to keep this mod vanilla flavored)
- Fixing whatever bugs you find that somehow I didn't find.

## Permissions

Yes, I would love to see this mod in your open source modpack. Curseforge packs are always okay, anywhere else, a link back to this page is appreciated. But you were already doing that for all mods anyway, right?

## Issues and Feature Requests

Please submit bugs and feature requests using the [Issues](https://github.com/dannydjdk/Tiny-Redstone/issues "Nutritional Balance issues") link above.
Be sure to include the mod version, forge version and any relevant crash reports.
This mod is in early beta, and therefore, may have some bugs and minor design issues.
If you do find any problems or have ideas for improvements, I would be most grateful for your input.

## Credits, Acknowledgements and Thanks

Inspiration for this mod comes from many mods and vanilla Redstone, of course. Probably at the top of the list is Super Circuit Maker. Although there are many differences, I have long since missed being able to create my own compact redstone circuits, and made this mod to fulfill that deep longing.
Thanks also goes to the Forge team and to the many mod authors who came before me.
Without their community contributions, this would not have been possible.