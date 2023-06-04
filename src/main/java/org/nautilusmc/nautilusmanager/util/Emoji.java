package org.nautilusmc.nautilusmanager.util;

import org.jetbrains.annotations.Nullable;

public enum Emoji {
//    ADI_SHAKTI("☬"),
    AIRPLANE("✈"),
//    ANKH("☥"),
//    AQUARIUS("♒"),
//    ARIES("♈"),
//    ASCENDING_NODE("☊"),
    AXE("\uD83E\uDE93"),
    BOX("☐"),
    BOX_CHECK("☑"),
    BOX_X("☒"),
//    BANGBANG("‼"),
    BIOHAZARD("☣"),
    BISHOP("♝"),
    BISHOP_OUTLINE("♗"),
//    BLACK_NIB("✒"),
    BOW("\uD83C\uDFF9"),
    C("©"),
    CADUCEUS("☤"),
//    CANCER("♋"),
//    CAPRICORN("♑"),
//    CAUTION("☡"),
    CHECK("✔"),
//    CHI_RHO("☧"),
    CIRCLED_M("Ⓜ"),
    CLOUD("☁"),
    CLUBS("♣"),
    CLUBS_OUTLINE("♧"),
    COMET("☄"),
//    CONGRATULATIONS("㊗"),
//    CONJUNCTION("☌"),
//    DESCENDING_NODE("☋"),
    DIAMONDS("♦"),
    DIAMONDS_OUTLINE("♢"),
    DOWN("↓"),
    DOWN_LEFT("↙"),
    DOWN_RIGHT("↘"),
//    EARTH("♁"),
    EIGHTH_NOTE("♪"),
    EIGHTH_NOTES("♫"),
    EIGHT_STAR("✴"),
    EIGHT_ASTERISK("✳"),
    ENVELOPE("✉"),
//    FARSI("☫"),
    FEMALE("♀"),
    FIRE("\uD83D\uDD25"),
    MOON("☽"),
    FISHING_ROD("\uD83C\uDFA3"),
    FLAT("♭"),
    FLORAL_HEART("❦"),
    FLORAL_HEART_BULLET("❧"),
    FROWN("☹"),
//    GEMINI("♊"),
//    HAMMER_SICKLE("☭"),
    HEART("❤"),
    HEARTS("♥"),
    HEARTS_OUTLINE("♡"),
    HEART_BULLET("❥"),
    HEART_EXCLAMATION("❣"),
//    HOT_SPRINGS("♨"),
    HOURGLASS("⌛"),
    INFINITY("∞"),
//    JERUSALEM_CROSS("☩"),
//    JUPITER("♃"),
//    KEYBOARD("⌨"),
    KING("♚"),
    KING_OUTLINE("♔"),
    KNIGHT("♞"),
    KNIGHT_OUTLINE("♘"),
//    LAST_QUARTER_MOON("☾"),
    CROSS("✝"),
    LEFT("←"),
    LEFT_RIGHT("⇄"),
    IFF("↔"),
//    LEO("♌"),
//    LIBRA("♎"),
//    LIGHTNING("☇"),
//    LORRAINE_CROSS("☨"),
    MALE("♂"),
//    MULTIPLY("✖"),
    NATURAL("♮"),
//    NEPTUNE("♆"),
//    OPPOSITION("☍"),
//    ORTHODOX_CROSS("☦"),
    PAWN("♟"),
    PAWN_OUTLINE("♙"),
    PEACE("☮"),
//    PENCIL_RIGHT("✏"),
    PENCIL("✎"),
//    PENCIL_UP_RIGHT("✐"),
    PI("π"),
    PICKAXE("⛏"),
//    PISCES("♓"),
    PLAY("▶"),
//    PLUTO("♇"),
//    POINT_DOWN("☟"),
//    POINT_LEFT("☚"),
    POINT_LEFT("☜"),
//    POINT_RIGHT("☛"),
    POINT_RIGHT("☞"),
//    POINT_UP("☝"),
    POTION("\uD83E\uDDEA"),
    QUARTER_NOTE("♩"),
    QUEEN("♛"),
    QUEEN_OUTLINE("♕"),
    R("®"),
    RADIOACTIVE("☢"),
    REVERSE("◀"),
    RIGHT("→"),
    ROOK("♜"),
    ROOK_OUTLINE("♖"),
//    SAGITTARIUS("♐"),
//    SALTIRE("☓"),
//    SATURN("♄"),
    SHEARS("✂"),
//    SCORPIO("♏"),
//    SECRET("㊙"),
    SHARP("♯"),
    SHIELD("\uD83D\uDEE1"),
    SIXTEENTH_NOTES("♬"),
    SKULL("☠"),
    SMALL_SQUARE("▪"),
    SMALL_SQUARE_OUTLINE("▫"),
    SMILE("☺"),
    SMILE_FACE("☻"),
    SNOWFLAKE("❄"),
    SNOWMAN("☃"),
    SPADES("♠"),
    SPADES_OUTLINE("♤"),
    SPARKLE("❇"),
    SPLASH_POTION("⚗"),
    STAR("★"),
    STAR_AND_CRESCENT("☪"),
    STAR_OF_DAVID("✡"),
    SUN("☀"),
//    SUN_OUTLINE("☼"),
//    SUN_SYMBOL("☉"),
    SWORD("\uD83D\uDDE1"),
    SWORDS("⚔"),
//    TAURUS("♉"),
//    TELEPHONE("☎"),
//    TELEPHONE_OUTLINE("☏"),
    THUNDERSTORM("☈"),
    TM("™"),
    TRIDENT("\uD83D\uDD31"),
    TRIGRAM_EARTH("☷"),
    TRIGRAM_FIRE("☲"),
    TRIGRAM_HEAVEN("☰"),
    TRIGRAM_LAKE("☱"),
    TRIGRAM_MOUNTAIN("☶"),
    TRIGRAM_THUNDER("☳"),
    TRIGRAM_WATER("☵"),
    TRIGRAM_WIND("☴"),
    TURN_LEFT("↩"),
    TURN_RIGHT("↪"),
    UMBRELLA("☂"),
    UP("↑"),
    UP_DOWN("⇵"),
    UP_LEFT("↖"),
    UP_RIGHT("↗"),
//    URANUS("♅"),
    V("✌"),
//    VIRGO("♍"),
    CLOCK("⌚"),
    WAVY_DASH("〰"),
//    WHEEL_OF_DHARMA("☸"),
    WRITING_HAND("✍"),
    X("❌"),
    YIN_YANG("☯");

    private final String raw;

    Emoji(String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public static @Nullable Emoji find(String id) {
        // sue me
        try {
            return valueOf(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String parseText(String text) {
        int start = text.indexOf(':'), end = -1;
        if (start < 0) {
            return text;
        }
        StringBuilder output = new StringBuilder(text.length()).append(text, 0, start);
        while (true) {
            end = text.indexOf(':', start + 1);
            if (end < 0) {
                output.append(text, start, text.length());
                break;
            }
            Emoji emoji = find(text.substring(start + 1, end).toUpperCase());
            if (emoji != null) {
                output.append(emoji.getRaw());
                start = text.indexOf(':', end + 1);
                if (start < 0) {
                    output.append(text, end + 1, text.length());
                    break;
                } else {
                    output.append(text, end + 1, start);
                }
            } else {
                output.append(text, start, end);
                start = end;
            }
        }
        return output.toString();
    }

}
