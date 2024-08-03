package me.tuan.uncraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Stream;

public class Uncraft extends JavaPlugin {

	@Override
	public void onEnable() {
		getServer().getPluginCommand(getClass().getSimpleName().toLowerCase()).setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		ItemStack current = player.getEquipment().getItemInMainHand();
		if (!valid(current)) return true;

		ItemStack[] ingredients = ingredients(current);
		if (ingredients != null && ingredients.length > 0) {
			dropItems(player.getLocation(), player.getInventory().addItem(ingredients));
			player.getEquipment().setItemInMainHand(null);
		}

		return true;
	}

	private boolean valid(ItemStack item) {
		return item.getType() != Material.AIR && !hasDamage(item.getItemMeta());
	}

	private boolean hasDamage(ItemMeta meta) {
		return meta != null && meta instanceof Damageable && ((Damageable) meta).hasDamage();
	}

	private void dropItems(Location location, Map<Integer, ItemStack> items) {
		items.forEach((index, item) -> location.getWorld().dropItemNaturally(location, item));
	}

	private ItemStack[] ingredients(ItemStack item) {
		Recipe recipe = getServer().getRecipe(item.getType().getKey());
		if (recipe != null) return Stream.of(recipe)
			.parallel()
			.flatMap(this::toIngredients)
			.filter(Objects::nonNull)
			.map(i -> fixAmount(i, item.getAmount()))
			.toArray(ItemStack[]::new);

		return null;
	}	

	private ItemStack fixAmount(ItemStack item, int amount) {
		item.setAmount(amount);
		return item;
	}

	private Stream<ItemStack> toIngredients(Recipe recipe) {
		if (recipe instanceof ShapedRecipe) return fromShapedRecipe(recipe);
		if (recipe instanceof ShapelessRecipe) return fromShapelessRecipe(recipe);
		return Stream.empty();
	}

	private Stream<ItemStack> fromShapedRecipe(Recipe recipe) {
		return ((ShapedRecipe) recipe).getIngredientMap().values().stream();
	}

	private Stream<ItemStack> fromShapelessRecipe(Recipe recipe) {
		return ((ShapelessRecipe) recipe).getIngredientList().stream();
	}
}