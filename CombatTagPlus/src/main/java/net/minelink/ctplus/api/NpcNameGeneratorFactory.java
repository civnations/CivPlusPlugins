package net.minelink.ctplus.api;

public final class NpcNameGeneratorFactory {

    private static NpcNameGenerator nameGenerator;

    public static NpcNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public static void setNameGenerator(NpcNameGenerator nameGenerator) {
        NpcNameGeneratorFactory.nameGenerator = nameGenerator;
    }

}
