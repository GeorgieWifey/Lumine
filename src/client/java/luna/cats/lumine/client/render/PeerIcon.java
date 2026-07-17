package luna.cats.lumine.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class PeerIcon {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("lumine",
			"textures/lunar_logo.png");

	private PeerIcon() {
	}

	public static void draw(GuiGraphicsExtractor graphics, int x, int y, int size, float r, float g,
			float b, float a) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, size, size, 100, 100,
				100, 100, ARGB.colorFromFloat(a, r, g, b));
	}
}
