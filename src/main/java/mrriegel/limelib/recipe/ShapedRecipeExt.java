package mrriegel.limelib.recipe;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;

import mrriegel.limelib.helper.RecipeHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedRecipeExt extends ShapedOreRecipe {

	public ShapedRecipeExt(ResourceLocation group, Block result, Object... recipe) {
		super(group, result, recipe);
	}

	public ShapedRecipeExt(ResourceLocation group, Item result, Object... recipe) {
		super(group, result, recipe);
	}

	public ShapedRecipeExt(ResourceLocation group, ItemStack result, Object... recipe) {
		super(group, result, getPrimer(recipe));
	}

	public ShapedRecipeExt(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
		super(group, result, primer);
	}

	private static ShapedPrimer getPrimer(Object... recipe) {
		ShapedPrimer ret = new ShapedPrimer();
		String shape = "";
		int idx = 0;

		if (recipe[idx] instanceof Boolean) {
			ret.mirrored = (Boolean) recipe[idx];
			if (recipe[idx + 1] instanceof Object[])
				recipe = (Object[]) recipe[idx + 1];
			else
				idx = 1;
		}

		if (recipe[idx] instanceof String[]) {
			String[] parts = ((String[]) recipe[idx++]);

			for (String s : parts) {
				ret.width = s.length();
				shape += s;
			}

			ret.height = parts.length;
		} else {
			while (recipe[idx] instanceof String) {
				String s = (String) recipe[idx++];
				shape += s;
				ret.width = s.length();
				ret.height++;
			}
		}

		if (ret.width * ret.height != shape.length() || shape.length() == 0) {
			String err = "Invalid shaped recipe: ";
			for (Object tmp : recipe) {
				err += tmp + ", ";
			}
			throw new RuntimeException(err);
		}

		HashMap<Character, Ingredient> itemMap = Maps.newHashMap();
		itemMap.put(' ', Ingredient.EMPTY);

		for (; idx < recipe.length; idx += 2) {
			Character chr = (Character) recipe[idx];
			Object in = recipe[idx + 1];
			Ingredient ing = RecipeHelper.getIngredient(in);

			if (' ' == chr.charValue())
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

			if (ing != null) {
				itemMap.put(chr, ing);
			} else {
				String err = "Invalid shaped ore recipe: ";
				for (Object tmp : recipe) {
					err += tmp + ", ";
				}
				throw new RuntimeException(err);
			}
		}

		ret.input = NonNullList.withSize(ret.width * ret.height, Ingredient.EMPTY);

		Set<Character> keys = Sets.newHashSet(itemMap.keySet());
		keys.remove(' ');

		int x = 0;
		for (char chr : shape.toCharArray()) {
			Ingredient ing = itemMap.get(chr);
			if (ing == null)
				throw new IllegalArgumentException("Pattern references symbol '" + chr + "' but it's not defined in the key");
			ret.input.set(x++, ing);
			keys.remove(chr);
		}

		if (!keys.isEmpty())
			throw new IllegalArgumentException("Key defines symbols that aren't used in pattern: " + keys);

		return ret;
	}
}