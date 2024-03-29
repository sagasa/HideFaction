package hide.region;

import hide.core.HideFaction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemRegionEdit extends Item {

	public ItemRegionEdit() {
		setRegistryName(HideFaction.MODID, "edit_region");/*登録名の設定*/
		setCreativeTab(CreativeTabs.MISC);/*クリエイティブのタブ*/
		setUnlocalizedName("editregion");
		/*翻訳キーの設定*/
		/*.setHasSubtypes(true)*//*ダメージ値等で複数の種類のアイテムを分けているかどうか。デフォルトfalse*/
		/*.setMaxDamage(256)*//*耐久値の設定。デフォルト0*/
		/*.setFull3D()*//*3D表示で描画させる。ツールや骨、棒等。*/
		/*.setContainerItem(Items.stick)*//*クラフト時にアイテムを返却できるようにしている際の返却アイテムの指定。*/
		/*.setPotionEffect(PotionHelper.ghastTearEffect)*//*指定文字列に対応した素材として醸造台で使える。PotionHelper参照のこと。*/
		/*.setNoRepair()*//*修理レシピを削除し、金床での修繕を出来なくする*/
		/*.setMaxStackSize(64)*//*スタックできる量。デフォルト64*/}
}
