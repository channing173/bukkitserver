package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Raid {

    public static final ItemStack a = H();
    private static final ChatMessage b = new ChatMessage("event.minecraft.raid", new Object[0]);
    private static final ChatMessage c = new ChatMessage("event.minecraft.raid.victory", new Object[0]);
    private static final ChatMessage d = new ChatMessage("event.minecraft.raid.defeat", new Object[0]);
    private static final IChatBaseComponent e = Raid.b.g().a(" - ").addSibling(Raid.c);
    private static final IChatBaseComponent f = Raid.b.g().a(" - ").addSibling(Raid.d);
    private final Map<Integer, EntityRaider> g = Maps.newHashMap();
    private final Map<Integer, Set<EntityRaider>> h = Maps.newHashMap();
    private final Set<UUID> i = Sets.newHashSet();
    private long j;
    private BlockPosition k;
    private final WorldServer l;
    private boolean m;
    private final int n;
    private float o;
    private int p;
    private boolean q;
    private int r;
    private final BossBattleServer s;
    private int t;
    private int u;
    private final Random v;
    private final int w;
    private Raid.Status x;
    private int y;
    private Optional<BlockPosition> z;

    public Raid(int i, WorldServer worldserver, BlockPosition blockposition) {
        this.s = new BossBattleServer(Raid.b, BossBattle.BarColor.RED, BossBattle.BarStyle.NOTCHED_10);
        this.v = new Random();
        this.z = Optional.empty();
        this.n = i;
        this.l = worldserver;
        this.q = true;
        this.u = 300;
        this.s.setProgress(0.0F);
        this.k = blockposition;
        this.w = this.a(worldserver.getDifficulty());
        this.x = Raid.Status.ONGOING;
    }

    public Raid(WorldServer worldserver, NBTTagCompound nbttagcompound) {
        this.s = new BossBattleServer(Raid.b, BossBattle.BarColor.RED, BossBattle.BarStyle.NOTCHED_10);
        this.v = new Random();
        this.z = Optional.empty();
        this.l = worldserver;
        this.n = nbttagcompound.getInt("Id");
        this.m = nbttagcompound.getBoolean("Started");
        this.q = nbttagcompound.getBoolean("Active");
        this.j = nbttagcompound.getLong("TicksActive");
        this.p = nbttagcompound.getInt("BadOmenLevel");
        this.r = nbttagcompound.getInt("GroupsSpawned");
        this.u = nbttagcompound.getInt("PreRaidTicks");
        this.t = nbttagcompound.getInt("PostRaidTicks");
        this.o = nbttagcompound.getFloat("TotalHealth");
        this.k = new BlockPosition(nbttagcompound.getInt("CX"), nbttagcompound.getInt("CY"), nbttagcompound.getInt("CZ"));
        this.w = nbttagcompound.getInt("NumGroups");
        this.x = Raid.Status.b(nbttagcompound.getString("Status"));
        this.i.clear();
        if (nbttagcompound.hasKeyOfType("HeroesOfTheVillage", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("HeroesOfTheVillage", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompound(i);
                UUID uuid = nbttagcompound1.a("UUID");

                this.i.add(uuid);
            }
        }

    }

    public boolean a() {
        return this.e() || this.f();
    }

    public boolean b() {
        return this.c() && this.r() == 0 && this.u > 0;
    }

    public boolean c() {
        return this.r > 0;
    }

    public boolean d() {
        return this.x == Raid.Status.STOPPED;
    }

    public boolean e() {
        return this.x == Raid.Status.VICTORY;
    }

    public boolean f() {
        return this.x == Raid.Status.LOSS;
    }

    public World i() {
        return this.l;
    }

    public boolean j() {
        return this.m;
    }

    public int k() {
        return this.r;
    }

    private Predicate<EntityPlayer> w() {
        return (entityplayer) -> {
            BlockPosition blockposition = new BlockPosition(entityplayer);

            return entityplayer.isAlive() && entityplayer.getWorldServer().b_(blockposition) && this.s().m(blockposition) <= 9216.0D;
        };
    }

    private void x() {
        Set<EntityPlayer> set = Sets.newHashSet();
        Iterator iterator = this.l.a(this.w()).iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            this.s.addPlayer(entityplayer);
            set.add(entityplayer);
        }

        Set<EntityPlayer> set1 = Sets.newHashSet(this.s.getPlayers());

        set1.removeAll(set);
        Iterator iterator1 = set1.iterator();

        while (iterator1.hasNext()) {
            EntityPlayer entityplayer1 = (EntityPlayer) iterator1.next();

            this.s.removePlayer(entityplayer1);
        }

    }

    public int l() {
        return 5;
    }

    public int m() {
        return this.p;
    }

    public void a(EntityHuman entityhuman) {
        if (entityhuman.hasEffect(MobEffects.BAD_OMEN)) {
            this.p += entityhuman.getEffect(MobEffects.BAD_OMEN).getAmplifier() + 1;
            this.p = MathHelper.clamp(this.p, 0, this.l());
        }

        entityhuman.removeEffect(MobEffects.BAD_OMEN);
    }

    public void n() {
        this.q = false;
        this.s.b();
        this.x = Raid.Status.STOPPED;
    }

    public void o() {
        if (!this.d()) {
            if (this.x == Raid.Status.ONGOING) {
                boolean flag = this.q;

                this.q = this.l.isLoaded(this.k);
                if (this.l.getDifficulty() == EnumDifficulty.PEACEFUL) {
                    this.n();
                    return;
                }

                if (flag != this.q) {
                    this.s.setVisible(this.q);
                }

                if (!this.q) {
                    return;
                }

                if (!this.l.b_(this.k)) {
                    this.y();
                }

                if (!this.l.b_(this.k)) {
                    if (this.r > 0) {
                        this.x = Raid.Status.LOSS;
                    } else {
                        this.n();
                    }
                }

                ++this.j;
                if (this.j >= 48000L) {
                    this.n();
                    return;
                }

                int i = this.r();
                boolean flag1;

                if (i == 0 && this.z()) {
                    if (this.u > 0) {
                        flag1 = this.z.isPresent();
                        boolean flag2 = !flag1 && this.u % 5 == 0;

                        if (flag1 && !this.l.getChunkProvider().a(new ChunkCoordIntPair((BlockPosition) this.z.get()))) {
                            flag2 = true;
                        }

                        if (flag2) {
                            byte b0 = 0;

                            if (this.u < 100) {
                                b0 = 1;
                            } else if (this.u < 40) {
                                b0 = 2;
                            }

                            this.z = this.d(b0);
                        }

                        if (this.u == 300 || this.u % 20 == 0) {
                            this.x();
                        }

                        --this.u;
                        this.s.setProgress(MathHelper.a((float) (300 - this.u) / 300.0F, 0.0F, 1.0F));
                    } else if (this.u == 0 && this.r > 0) {
                        this.u = 300;
                        this.s.a((IChatBaseComponent) Raid.b);
                        return;
                    }
                }

                if (this.j % 20L == 0L) {
                    this.x();
                    this.E();
                    if (i > 0) {
                        if (i <= 2) {
                            this.s.a(Raid.b.g().a(" - ").addSibling(new ChatMessage("event.minecraft.raid.raiders_remaining", new Object[]{i})));
                        } else {
                            this.s.a((IChatBaseComponent) Raid.b);
                        }
                    } else {
                        this.s.a((IChatBaseComponent) Raid.b);
                    }
                }

                flag1 = false;
                int j = 0;

                while (this.F()) {
                    BlockPosition blockposition = this.z.isPresent() ? (BlockPosition) this.z.get() : this.a(j, 20);

                    if (blockposition != null) {
                        this.m = true;
                        this.b(blockposition);
                        if (!flag1) {
                            this.a(blockposition);
                            flag1 = true;
                        }
                    } else {
                        ++j;
                    }

                    if (j > 3) {
                        this.n();
                        break;
                    }
                }

                if (this.j() && !this.z() && i == 0) {
                    if (this.t < 40) {
                        ++this.t;
                    } else {
                        this.x = Raid.Status.VICTORY;
                        Iterator iterator = this.i.iterator();

                        while (iterator.hasNext()) {
                            UUID uuid = (UUID) iterator.next();
                            Entity entity = this.l.getEntity(uuid);

                            if (entity instanceof EntityLiving && !entity.t()) {
                                EntityLiving entityliving = (EntityLiving) entity;

                                entityliving.addEffect(new MobEffect(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.p - 1, false, false, true));
                                if (entityliving instanceof EntityPlayer) {
                                    EntityPlayer entityplayer = (EntityPlayer) entityliving;

                                    entityplayer.a(StatisticList.RAID_WIN);
                                    CriterionTriggers.H.a(entityplayer);
                                }
                            }
                        }
                    }
                }

                this.G();
            } else if (this.a()) {
                ++this.y;
                if (this.y >= 600) {
                    this.n();
                    return;
                }

                if (this.y % 20 == 0) {
                    this.x();
                    this.s.setVisible(true);
                    if (this.e()) {
                        this.s.setProgress(0.0F);
                        this.s.a(Raid.e);
                    } else {
                        this.s.a(Raid.f);
                    }
                }
            }

        }
    }

    private void y() {
        Stream<SectionPosition> stream = SectionPosition.a(SectionPosition.a(this.k), 2);
        WorldServer worldserver = this.l;

        this.l.getClass();
        stream.filter(worldserver::a).map(SectionPosition::t).min(Comparator.comparingDouble((blockposition) -> {
            return blockposition.m(this.k);
        })).ifPresent(this::c);
    }

    private Optional<BlockPosition> d(int i) {
        for (int j = 0; j < 3; ++j) {
            BlockPosition blockposition = this.a(i, 1);

            if (blockposition != null) {
                return Optional.of(blockposition);
            }
        }

        return Optional.empty();
    }

    private boolean z() {
        return this.B() ? !this.C() : !this.A();
    }

    private boolean A() {
        return this.k() == this.w;
    }

    private boolean B() {
        return this.p > 1;
    }

    private boolean C() {
        return this.k() > this.w;
    }

    private boolean D() {
        return this.A() && this.r() == 0 && this.B();
    }

    private void E() {
        Iterator<Set<EntityRaider>> iterator = this.h.values().iterator();
        HashSet hashset = Sets.newHashSet();

        while (iterator.hasNext()) {
            Set<EntityRaider> set = (Set) iterator.next();
            Iterator iterator1 = set.iterator();

            while (iterator1.hasNext()) {
                EntityRaider entityraider = (EntityRaider) iterator1.next();
                BlockPosition blockposition = new BlockPosition(entityraider);

                if (!entityraider.dead && entityraider.dimension == this.l.getWorldProvider().getDimensionManager() && this.k.m(blockposition) < 12544.0D) {
                    if (entityraider.ticksLived > 600) {
                        if (this.l.getEntity(entityraider.getUniqueID()) == null) {
                            hashset.add(entityraider);
                        }

                        if (!this.l.b_(blockposition) && entityraider.cv() > 2400) {
                            entityraider.b(entityraider.eo() + 1);
                        }

                        if (entityraider.eo() >= 30) {
                            hashset.add(entityraider);
                        }
                    }
                } else {
                    hashset.add(entityraider);
                }
            }
        }

        Iterator iterator2 = hashset.iterator();

        while (iterator2.hasNext()) {
            EntityRaider entityraider1 = (EntityRaider) iterator2.next();

            this.a(entityraider1, true);
        }

    }

    private void a(BlockPosition blockposition) {
        float f = 13.0F;
        boolean flag = true;
        Iterator iterator = this.l.getPlayers().iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();
            Vec3D vec3d = new Vec3D(entityhuman.locX, entityhuman.locY, entityhuman.locZ);
            Vec3D vec3d1 = new Vec3D((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
            float f1 = MathHelper.sqrt((vec3d1.x - vec3d.x) * (vec3d1.x - vec3d.x) + (vec3d1.z - vec3d.z) * (vec3d1.z - vec3d.z));
            double d0 = vec3d.x + (double) (13.0F / f1) * (vec3d1.x - vec3d.x);
            double d1 = vec3d.z + (double) (13.0F / f1) * (vec3d1.z - vec3d.z);

            if (f1 <= 64.0F || this.l.b_(new BlockPosition(entityhuman))) {
                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(SoundEffects.EVENT_RAID_HORN, SoundCategory.NEUTRAL, d0, entityhuman.locY, d1, 64.0F, 1.0F));
            }
        }

    }

    private void b(BlockPosition blockposition) {
        boolean flag = false;
        int i = this.r + 1;

        this.o = 0.0F;
        DifficultyDamageScaler difficultydamagescaler = this.l.getDamageScaler(blockposition);
        boolean flag1 = this.D();
        Raid.Wave[] araid_wave = Raid.Wave.f;
        int j = araid_wave.length;

        for (int k = 0; k < j; ++k) {
            Raid.Wave raid_wave = araid_wave[k];
            int l = this.a(raid_wave, i, flag1) + this.a(raid_wave, this.v, i, difficultydamagescaler, flag1);
            int i1 = 0;

            for (int j1 = 0; j1 < l; ++j1) {
                EntityRaider entityraider = (EntityRaider) raid_wave.g.a((World) this.l);

                this.a(i, entityraider, blockposition, false);
                if (!flag && entityraider.dY()) {
                    entityraider.setPatrolLeader(true);
                    this.a(i, entityraider);
                    flag = true;
                }

                if (raid_wave.g == EntityTypes.RAVAGER) {
                    EntityRaider entityraider1 = null;

                    if (i == this.a(EnumDifficulty.NORMAL)) {
                        entityraider1 = (EntityRaider) EntityTypes.PILLAGER.a((World) this.l);
                    } else if (i >= this.a(EnumDifficulty.HARD)) {
                        if (i1 == 0) {
                            entityraider1 = (EntityRaider) EntityTypes.EVOKER.a((World) this.l);
                        } else {
                            entityraider1 = (EntityRaider) EntityTypes.VINDICATOR.a((World) this.l);
                        }
                    }

                    ++i1;
                    if (entityraider1 != null) {
                        this.a(i, entityraider1, blockposition, false);
                        entityraider1.setPositionRotation(blockposition, 0.0F, 0.0F);
                        entityraider1.startRiding(entityraider);
                    }
                }
            }
        }

        this.z = Optional.empty();
        ++this.r;
        this.p();
        this.G();
    }

    public void a(int i, EntityRaider entityraider, @Nullable BlockPosition blockposition, boolean flag) {
        boolean flag1 = this.b(i, entityraider);

        if (flag1) {
            entityraider.a(this);
            entityraider.a(i);
            entityraider.t(true);
            entityraider.b(0);
            if (!flag && blockposition != null) {
                entityraider.setPosition((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 1.0D, (double) blockposition.getZ() + 0.5D);
                entityraider.prepare(this.l, this.l.getDamageScaler(blockposition), EnumMobSpawn.EVENT, (GroupDataEntity) null, (NBTTagCompound) null);
                entityraider.a(i, false);
                entityraider.onGround = true;
                this.l.addEntity(entityraider);
            }
        }

    }

    public void p() {
        this.s.setProgress(MathHelper.a(this.q() / this.o, 0.0F, 1.0F));
    }

    public float q() {
        float f = 0.0F;
        Iterator iterator = this.h.values().iterator();

        while (iterator.hasNext()) {
            Set<EntityRaider> set = (Set) iterator.next();

            EntityRaider entityraider;

            for (Iterator iterator1 = set.iterator(); iterator1.hasNext(); f += entityraider.getHealth()) {
                entityraider = (EntityRaider) iterator1.next();
            }
        }

        return f;
    }

    private boolean F() {
        return this.u == 0 && (this.r < this.w || this.D()) && this.r() == 0;
    }

    public int r() {
        return this.h.values().stream().mapToInt(Set::size).sum();
    }

    public void a(@Nonnull EntityRaider entityraider, boolean flag) {
        Set<EntityRaider> set = (Set) this.h.get(entityraider.em());

        if (set != null) {
            boolean flag1 = set.remove(entityraider);

            if (flag1) {
                if (flag) {
                    this.o -= entityraider.getHealth();
                }

                entityraider.a((Raid) null);
                this.p();
                this.G();
            }
        }

    }

    private void G() {
        this.l.C().b();
    }

    private static ItemStack H() {
        ItemStack itemstack = new ItemStack(Items.WHITE_BANNER);
        NBTTagCompound nbttagcompound = itemstack.a("BlockEntityTag");
        NBTTagList nbttaglist = (new EnumBannerPatternType.a()).a(EnumBannerPatternType.RHOMBUS_MIDDLE, EnumColor.CYAN).a(EnumBannerPatternType.STRIPE_BOTTOM, EnumColor.LIGHT_GRAY).a(EnumBannerPatternType.STRIPE_CENTER, EnumColor.GRAY).a(EnumBannerPatternType.BORDER, EnumColor.LIGHT_GRAY).a(EnumBannerPatternType.STRIPE_MIDDLE, EnumColor.BLACK).a(EnumBannerPatternType.HALF_HORIZONTAL, EnumColor.LIGHT_GRAY).a(EnumBannerPatternType.CIRCLE_MIDDLE, EnumColor.LIGHT_GRAY).a(EnumBannerPatternType.BORDER, EnumColor.BLACK).a();

        nbttagcompound.set("Patterns", nbttaglist);
        itemstack.a((new ChatMessage("block.minecraft.ominous_banner", new Object[0])).a(EnumChatFormat.GOLD));
        return itemstack;
    }

    @Nullable
    public EntityRaider b(int i) {
        return (EntityRaider) this.g.get(i);
    }

    @Nullable
    private BlockPosition a(int i, int j) {
        int k = i == 0 ? 2 : 2 - i;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int l = 0; l < j; ++l) {
            float f = this.l.random.nextFloat() * 6.2831855F;
            int i1 = this.k.getX() + MathHelper.d(MathHelper.cos(f) * 32.0F * (float) k) + this.l.random.nextInt(5);
            int j1 = this.k.getZ() + MathHelper.d(MathHelper.sin(f) * 32.0F * (float) k) + this.l.random.nextInt(5);
            int k1 = this.l.a(HeightMap.Type.WORLD_SURFACE, i1, j1);

            blockposition_mutableblockposition.d(i1, k1, j1);
            if ((!this.l.b_(blockposition_mutableblockposition) || i >= 2) && this.l.isAreaLoaded(blockposition_mutableblockposition.getX() - 10, blockposition_mutableblockposition.getY() - 10, blockposition_mutableblockposition.getZ() - 10, blockposition_mutableblockposition.getX() + 10, blockposition_mutableblockposition.getY() + 10, blockposition_mutableblockposition.getZ() + 10) && this.l.getChunkProvider().a(new ChunkCoordIntPair(blockposition_mutableblockposition)) && (SpawnerCreature.a(EntityPositionTypes.Surface.ON_GROUND, (IWorldReader) this.l, (BlockPosition) blockposition_mutableblockposition, EntityTypes.RAVAGER) || this.l.getType(blockposition_mutableblockposition.down()).getBlock() == Blocks.SNOW && this.l.getType(blockposition_mutableblockposition).isAir())) {
                return blockposition_mutableblockposition;
            }
        }

        return null;
    }

    private boolean b(int i, EntityRaider entityraider) {
        return this.a(i, entityraider, true);
    }

    public boolean a(int i, EntityRaider entityraider, boolean flag) {
        this.h.computeIfAbsent(i, (integer) -> {
            return Sets.newHashSet();
        });
        Set<EntityRaider> set = (Set) this.h.get(i);
        EntityRaider entityraider1 = null;
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            EntityRaider entityraider2 = (EntityRaider) iterator.next();

            if (entityraider2.getUniqueID().equals(entityraider.getUniqueID())) {
                entityraider1 = entityraider2;
                break;
            }
        }

        if (entityraider1 != null) {
            set.remove(entityraider1);
            set.add(entityraider);
        }

        set.add(entityraider);
        if (flag) {
            this.o += entityraider.getHealth();
        }

        this.p();
        this.G();
        return true;
    }

    public void a(int i, EntityRaider entityraider) {
        this.g.put(i, entityraider);
        entityraider.setSlot(EnumItemSlot.HEAD, Raid.a);
        entityraider.a(EnumItemSlot.HEAD, 2.0F);
    }

    public void c(int i) {
        this.g.remove(i);
    }

    public BlockPosition s() {
        return this.k;
    }

    private void c(BlockPosition blockposition) {
        this.k = blockposition;
    }

    public int t() {
        return this.n;
    }

    private int a(Raid.Wave raid_wave, int i, boolean flag) {
        return flag ? raid_wave.h[this.w] : raid_wave.h[i];
    }

    private int a(Raid.Wave raid_wave, Random random, int i, DifficultyDamageScaler difficultydamagescaler, boolean flag) {
        EnumDifficulty enumdifficulty = difficultydamagescaler.a();
        boolean flag1 = enumdifficulty == EnumDifficulty.EASY;
        boolean flag2 = enumdifficulty == EnumDifficulty.NORMAL;
        int j;

        switch (raid_wave) {
            case WITCH:
                if (flag1 || i <= 2 || i == 4) {
                    return 0;
                }

                j = 1;
                break;
            case PILLAGER:
            case VINDICATOR:
                if (flag1) {
                    j = random.nextInt(2);
                } else if (flag2) {
                    j = 1;
                } else {
                    j = 2;
                }
                break;
            case RAVAGER:
                j = !flag1 && flag ? 1 : 0;
                break;
            default:
                return 0;
        }

        return j > 0 ? random.nextInt(j + 1) : 0;
    }

    public boolean u() {
        return this.q;
    }

    public NBTTagCompound a(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("Id", this.n);
        nbttagcompound.setBoolean("Started", this.m);
        nbttagcompound.setBoolean("Active", this.q);
        nbttagcompound.setLong("TicksActive", this.j);
        nbttagcompound.setInt("BadOmenLevel", this.p);
        nbttagcompound.setInt("GroupsSpawned", this.r);
        nbttagcompound.setInt("PreRaidTicks", this.u);
        nbttagcompound.setInt("PostRaidTicks", this.t);
        nbttagcompound.setFloat("TotalHealth", this.o);
        nbttagcompound.setInt("NumGroups", this.w);
        nbttagcompound.setString("Status", this.x.a());
        nbttagcompound.setInt("CX", this.k.getX());
        nbttagcompound.setInt("CY", this.k.getY());
        nbttagcompound.setInt("CZ", this.k.getZ());
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = this.i.iterator();

        while (iterator.hasNext()) {
            UUID uuid = (UUID) iterator.next();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            nbttagcompound1.a("UUID", uuid);
            nbttaglist.add(nbttagcompound1);
        }

        nbttagcompound.set("HeroesOfTheVillage", nbttaglist);
        return nbttagcompound;
    }

    public int a(EnumDifficulty enumdifficulty) {
        switch (enumdifficulty) {
            case EASY:
                return 3;
            case NORMAL:
                return 5;
            case HARD:
                return 7;
            default:
                return 0;
        }
    }

    public float v() {
        int i = this.m();

        return i == 2 ? 0.1F : (i == 3 ? 0.25F : (i == 4 ? 0.5F : (i == 5 ? 0.75F : 0.0F)));
    }

    public void a(Entity entity) {
        this.i.add(entity.getUniqueID());
    }

    static enum Wave {

        VINDICATOR(EntityTypes.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}), EVOKER(EntityTypes.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}), PILLAGER(EntityTypes.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}), WITCH(EntityTypes.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}), RAVAGER(EntityTypes.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        private static final Raid.Wave[] f = values();
        private final EntityTypes<? extends EntityRaider> g;
        private final int[] h;

        private Wave(EntityTypes entitytypes, int[] aint) {
            this.g = entitytypes;
            this.h = aint;
        }
    }

    static enum Status {

        ONGOING, VICTORY, LOSS, STOPPED;

        private static final Raid.Status[] e = values();

        private Status() {}

        private static Raid.Status b(String s) {
            Raid.Status[] araid_status = Raid.Status.e;
            int i = araid_status.length;

            for (int j = 0; j < i; ++j) {
                Raid.Status raid_status = araid_status[j];

                if (s.equalsIgnoreCase(raid_status.name())) {
                    return raid_status;
                }
            }

            return Raid.Status.ONGOING;
        }

        public String a() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
