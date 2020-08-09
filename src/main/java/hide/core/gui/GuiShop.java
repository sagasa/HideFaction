package hide.core.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiShop extends GuiScreen {

    //private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation(HideShopMod.MODID,"book.png");
    private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("textures/gui/book.png");
    //private static final ResourceLocation BOOK_GUI_TEXTURES2 = new ResourceLocation(HideShopMod.MODID,"sample.png");
    //GuiButton buttonDone;
    GuiButton buttonNextPage;
    GuiButton buttonPreviousPage;
    GuiButton buttonBuyx1;
    GuiButton buttonBuyx16;
    GuiButton buttonBuyx64;

    GuiButton buttonSellx1;
    GuiButton buttonSellx16;
    GuiButton buttonSellx64;
    FontRenderer font;
    List<ItemStack> shopItems;
    EntityPlayer player;
    protected int pageCount = 0;

    public GuiShop(List<ItemStack> items, EntityPlayer player){
        //this.drawDefaultBackground();
        //this.setGuiSize(1000, 500);
        //ShopButton button = new ShopButton(10,100,100,100,100,"test");
        shopItems = items;
        this.player = player;
        }

    @Override
    public void initGui() {
        int i = (this.width - 192) / 2;
        //this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 - 72, 196, 140, 20, I18n.format("gui.done")));
        this.buttonNextPage = this.addButton(new GuiButton(1, i + 120, 156, 30,20,"Next"));
        this.buttonPreviousPage = this.addButton(new GuiButton(2, i + 38, 156, 30,20,"Prev"));

        this.buttonBuyx1 = this.addButton(new GuiButton(3, (this.width - 80)/ 2, 90, 20,20,"x1"));
        this.buttonBuyx16 = this.addButton(new GuiButton(4, (this.width - 80)/ 2, 110, 20,20,"x16"));
        this.buttonBuyx64 = this.addButton(new GuiButton(5, (this.width - 80)/ 2, 130, 20,20,"x64"));
        this.buttonSellx1 = this.addButton(new GuiButton(6, (this.width + 20) / 2, 90, 20,20,"x1"));
        this.buttonSellx16 = this.addButton(new GuiButton(7, (this.width + 20) / 2, 110, 20,20,"x16"));
        this.buttonSellx64 = this.addButton(new GuiButton(8, (this.width + 20) / 2, 130, 20,20,"x64"));

        //this.drawCenteredString(font,"Stock",(this.width - 20) / 2,60,0);
        //this.mc.fontRenderer.drawString("Stock",(this.width - 20) / 2,60,0);
        //GlStateManager.disableLighting();
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
        //this.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES2);
        this.drawTexturedModalRect((this.width - 192) / 2, 2, 0, 0, 192, 192);
        //this.drawTexturedModalRect(0, 0, 0, 0, 270, 230);

        //GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        //GlStateManager.disableLighting();
        //GlStateManager.enableRescaleNormal();
        //GlStateManager.enableColorMaterial();
        //GlStateManager.enableLighting();
        ItemStack items = shopItems.get(pageCount);
        this.itemRender.renderItemAndEffectIntoGUI(items,(this.width - 20)/ 2,20);
        this.fontRenderer.drawString("Stock",(this.width - 80) / 2,40,0);
        this.fontRenderer.drawString("Invt.",(this.width + 20) / 2,40,0);
        this.fontRenderer.drawString("Buy",(this.width - 80) / 2,80,0);
        this.fontRenderer.drawString("Sell",(this.width + 20) / 2,80,0);
        this.fontRenderer.drawString(String.valueOf(items.getCount()),(this.width - 80) / 2,50,0);
        int itemCount = 0;
        for(int n=0;n<player.inventory.mainInventory.size();n++){
            if(player.inventory.mainInventory.get(n).getItem().equals(shopItems.get(pageCount).getItem())){
                itemCount += player.inventory.mainInventory.get(n).getCount();
            }
        }
        this.fontRenderer.drawString(String.valueOf(itemCount), (this.width + 20) / 2, 50, 0);

        this.fontRenderer.drawString((pageCount+1)+"/"+shopItems.size(),(this.width + 60) / 2,15,0);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed()
    {

    }
    public void pageCountAdd(){
        if(this.pageCount<shopItems.size()-1){
            this.pageCount++;
            //System.out.println(pageCount);
        }else{
            System.out.println("page ended");
        }
    }
    public void pageCountSubtract(){
        if(this.pageCount > 0){
            this.pageCount--;
        }else{
            System.out.println("first page");
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button){

        int itemCount = 0;
        for(int n=0;n<player.inventory.mainInventory.size();n++){
            if(player.inventory.mainInventory.get(n).getItem().equals(shopItems.get(pageCount).getItem())){
                itemCount += player.inventory.mainInventory.get(n).getCount();
            }
        }

        switch (button.id) {
            case 0:
                System.out.println("done button clicked");
                break;
            case 1:
                System.out.println("next button clicked");
                this.pageCountAdd();
                break;
            case 2:
                System.out.println("prev button clicked");
                this.pageCountSubtract();
                break;
            case 3:
                buyItems(1);
                break;
            case 4:
                buyItems(16);
                break;
            case 5:
                buyItems(64);
                break;
            case 6:
                sellItems(itemCount, 1);
                break;
            case 7:
                sellItems(itemCount, 16);
                break;
            case 8:
                sellItems(itemCount, 64);
                break;
        }
    }

    public boolean sellItems(int itemCount, int sellCount) {
        if (itemCount >= sellCount) {
                for (int n = 0; n < player.inventory.mainInventory.size(); n++) {
                    if (player.inventory.mainInventory.get(n).getItem().equals(shopItems.get(pageCount).getItem())) {
                        if (player.inventory.mainInventory.get(n).getCount() > sellCount) {
                            player.inventory.mainInventory.get(n).setCount(player.inventory.mainInventory.get(n).getCount() - sellCount);
                            sellCount = 0;
                            System.out.println("sellcount:" + sellCount);
                            break;
                        } else {
                            sellCount = sellCount - player.inventory.mainInventory.get(n).getCount();
                            player.inventory.mainInventory.get(n).setCount(0);
                            System.out.println("sellcount:" + sellCount);
                        }
                    }
                }

            return true;
        } else {
            System.out.println("Not Enough Items!");
            return false;
        }
    }

    public boolean buyItems(int buyCount){
        return true;
    }

}
