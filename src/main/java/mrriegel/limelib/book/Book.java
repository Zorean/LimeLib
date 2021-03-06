package mrriegel.limelib.book;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import mrriegel.limelib.gui.GuiDrawer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class Book {

	protected List<Chapter> chapters = Lists.newArrayList();
	public Chapter lastChapter;
	public Article lastArticle;
	public int lastPage;

	public void addChapter(Chapter c) {
		c.index = chapters.size();
		chapters.add(c);
	}

	public Pair<Integer, Integer> getPage(IForgeRegistryEntry<?> impl) {
		if (impl == null)
			return null;
		for (Chapter c : chapters) {
			if (c.implMap.get(impl) != null) {
				return ImmutablePair.<Integer, Integer> of(c.index, c.implMap.get(impl).index);
			} else if (impl instanceof Item && Block.getBlockFromItem((Item) impl) != null && c.implMap.get(Block.getBlockFromItem((Item) impl)) != null) {
				return ImmutablePair.<Integer, Integer> of(c.index, c.implMap.get(Block.getBlockFromItem((Item) impl)).index);
			}
		}
		return null;
	}

	public void openGUI() {
		openGUI(-1, -1);
	};

	public void openGuiAt(IForgeRegistryEntry<?> impl, boolean openAnyway) {
		Pair<Integer, Integer> p = getPage(impl);
		if (p != null)
			openGUI(p.getLeft(), p.getRight());
		else if (openAnyway)
			openGUI();
	}

	public void openGUI(int chapter, int article) {
		GuiDrawer.openGui(new GuiBook(this, chapter, article));
	}

	public boolean canOpen(IForgeRegistryEntry<?> impl) {
		return getPage(impl) != null;
	}

}
