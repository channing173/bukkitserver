package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Lectern;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
// CraftBukkit end

public class TileEntityLectern extends TileEntity implements Clearable, ITileInventory, ICommandListener { // CraftBukkit - ICommandListener

    public final IInventory inventory = new IInventory() {
        // CraftBukkit start - add fields and methods
        public List<HumanEntity> transaction = new ArrayList<>();
        private int maxStack = 1;

        @Override
        public List<ItemStack> getContents() {
            return Arrays.asList(book);
        }

        @Override
        public void onOpen(CraftHumanEntity who) {
            transaction.add(who);
        }

        @Override
        public void onClose(CraftHumanEntity who) {
            transaction.remove(who);
        }

        @Override
        public List<HumanEntity> getViewers() {
            return transaction;
        }

        @Override
        public void setMaxStackSize(int i) {
            maxStack = i;
        }

        @Override
        public Location getLocation() {
            return new Location(world.getWorld(), position.getX(), position.getY(), position.getZ());
        }

        @Override
        public InventoryHolder getOwner() {
            return (Lectern) CraftBlock.at(world, position).getState();
        }
        // CraftBukkit end

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public boolean isNotEmpty() {
            return TileEntityLectern.this.book.isEmpty();
        }

        @Override
        public ItemStack getItem(int i) {
            return i == 0 ? TileEntityLectern.this.book : ItemStack.a;
        }

        @Override
        public ItemStack splitStack(int i, int j) {
            if (i == 0) {
                ItemStack itemstack = TileEntityLectern.this.book.cloneAndSubtract(j);

                if (TileEntityLectern.this.book.isEmpty()) {
                    TileEntityLectern.this.t();
                }

                return itemstack;
            } else {
                return ItemStack.a;
            }
        }

        @Override
        public ItemStack splitWithoutUpdate(int i) {
            if (i == 0) {
                ItemStack itemstack = TileEntityLectern.this.book;

                TileEntityLectern.this.book = ItemStack.a;
                TileEntityLectern.this.t();
                return itemstack;
            } else {
                return ItemStack.a;
            }
        }

        @Override
        public void setItem(int i, ItemStack itemstack) {}

        @Override
        public int getMaxStackSize() {
            return maxStack; // CraftBukkit
        }

        @Override
        public void update() {
            TileEntityLectern.this.update();
        }

        @Override
        public boolean a(EntityHuman entityhuman) {
            return TileEntityLectern.this.world.getTileEntity(TileEntityLectern.this.position) != TileEntityLectern.this ? false : (entityhuman.e((double) TileEntityLectern.this.position.getX() + 0.5D, (double) TileEntityLectern.this.position.getY() + 0.5D, (double) TileEntityLectern.this.position.getZ() + 0.5D) > 64.0D ? false : TileEntityLectern.this.f());
        }

        @Override
        public boolean b(int i, ItemStack itemstack) {
            return false;
        }

        @Override
        public void clear() {}
    };
    private final IContainerProperties containerProperties = new IContainerProperties() {
        @Override
        public int getProperty(int i) {
            return i == 0 ? TileEntityLectern.this.page : 0;
        }

        @Override
        public void setProperty(int i, int j) {
            if (i == 0) {
                TileEntityLectern.this.setPage(j);
            }

        }

        @Override
        public int a() {
            return 1;
        }
    };
    private ItemStack book;
    private int page;
    private int maxPage;

    public TileEntityLectern() {
        super(TileEntityTypes.LECTERN);
        this.book = ItemStack.a;
    }

    public ItemStack c() {
        return this.book;
    }

    public boolean f() {
        Item item = this.book.getItem();

        return item == Items.WRITABLE_BOOK || item == Items.WRITTEN_BOOK;
    }

    public void a(ItemStack itemstack) {
        this.a(itemstack, (EntityHuman) null);
    }

    private void t() {
        this.page = 0;
        this.maxPage = 0;
        BlockLectern.a(this.getWorld(), this.getPosition(), this.getBlock(), false);
    }

    public void a(ItemStack itemstack, @Nullable EntityHuman entityhuman) {
        this.book = this.b(itemstack, entityhuman);
        this.page = 0;
        this.maxPage = ItemWrittenBook.j(this.book);
        this.update();
    }

    public void setPage(int i) {
        int j = MathHelper.clamp(i, 0, this.maxPage - 1);

        if (j != this.page) {
            this.page = j;
            this.update();
            BlockLectern.a(this.getWorld(), this.getPosition(), this.getBlock());
        }

    }

    public int getPage() {
        return this.page;
    }

    public int s() {
        float f = this.maxPage > 1 ? (float) this.getPage() / ((float) this.maxPage - 1.0F) : 1.0F;

        return MathHelper.d(f * 14.0F) + (this.f() ? 1 : 0);
    }

    private ItemStack b(ItemStack itemstack, @Nullable EntityHuman entityhuman) {
        if (this.world instanceof WorldServer && itemstack.getItem() == Items.WRITTEN_BOOK) {
            ItemWrittenBook.a(itemstack, this.a(entityhuman), entityhuman);
        }

        return itemstack;
    }

    // CraftBukkit start
    @Override
    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
    }

    @Override
    public org.bukkit.command.CommandSender getBukkitSender(CommandListenerWrapper wrapper) {
        return wrapper.getEntity() != null ? wrapper.getEntity().getBukkitSender(wrapper) : new org.bukkit.craftbukkit.command.CraftBlockCommandSender(wrapper, this);
    }

    @Override
    public boolean shouldSendSuccess() {
        return false;
    }

    @Override
    public boolean shouldSendFailure() {
        return false;
    }

    @Override
    public boolean shouldBroadcastCommands() {
        return false;
    }

    // CraftBukkit end
    private CommandListenerWrapper a(@Nullable EntityHuman entityhuman) {
        String s;
        Object object;

        if (entityhuman == null) {
            s = "Lectern";
            object = new ChatComponentText("Lectern");
        } else {
            s = entityhuman.getDisplayName().getString();
            object = entityhuman.getScoreboardDisplayName();
        }

        Vec3D vec3d = new Vec3D((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D);

        // CraftBukkit - this
        return new CommandListenerWrapper(this, vec3d, Vec2F.a, (WorldServer) this.world, 2, s, (IChatBaseComponent) object, this.world.getMinecraftServer(), entityhuman);
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Book", 10)) {
            this.book = this.b(ItemStack.a(nbttagcompound.getCompound("Book")), (EntityHuman) null);
        } else {
            this.book = ItemStack.a;
        }

        this.maxPage = ItemWrittenBook.j(this.book);
        this.page = MathHelper.clamp(nbttagcompound.getInt("Page"), 0, this.maxPage - 1);
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        if (!this.c().isEmpty()) {
            nbttagcompound.set("Book", this.c().save(new NBTTagCompound()));
            nbttagcompound.setInt("Page", this.page);
        }

        return nbttagcompound;
    }

    @Override
    public void clear() {
        this.a(ItemStack.a);
    }

    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        return new ContainerLectern(i, this.inventory, this.containerProperties, playerinventory); // CraftBukkit
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatMessage("container.lectern", new Object[0]);
    }
}
