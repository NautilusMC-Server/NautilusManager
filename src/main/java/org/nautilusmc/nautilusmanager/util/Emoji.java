package org.nautilusmc.nautilusmanager.util;

import org.jetbrains.annotations.Nullable;

public enum Emoji {
    ADI_SHAKTI('☬'),
    AIRPLANE('✈'),
    ANKH('☥'),
    AQUARIUS('♒'),
    ARIES('♈'),
    ASCENDING_NODE('☊'),
    BALLOT_BOX('☐'),
    BALLOT_BOX_CHECKED('☑'),
    BALLOT_BOX_MARKED('☒'),
    BANGBANG('‼'),
    BIOHAZARD('☣'),
    BISHOP('♝'),
    BISHOP_OUTLINE('♗'),
    BLACK_NIB('✒'),
    C('©'),
    CADUCEUS('☤'),
    CANCER('♋'),
    CAPRICORN('♑'),
    CAUTION('☡'),
    CHECK('✔'),
    CHI_RHO('☧'),
    CIRCLED_M('Ⓜ'),
    CLOUD('☁'),
    CLUBS('♣'),
    CLUBS_OUTLINE('♧'),
    COMET('☄'),
    CONGRATULATIONS('㊗'),
    CONJUNCTION('☌'),
    DESCENDING_NODE('☋'),
    DIAMONDS('♦'),
    DIAMONDS_OUTLINE('♢'),
    DOWN('⬇'),
    DOWN_LEFT('↙'),
    DOWN_RIGHT('↘'),
    EARTH('♁'),
    EIGHT_POINTED_STAR('✴'),
    EIGHT_SPOKED_ASTERISK('✳'),
    EIGHTH_NOTE('♪'),
    EIGHTH_NOTES('♫'),
    ENVELOPE('✉'),
    FARSI('☫'),
    FEMALE('♀'),
    FIRST_QUARTER_MOON('☽'),
    FLAT('♭'),
    FLORAL_HEART('❦'),
    FLORAL_HEART_BULLET('❧'),
    FROWN('☹'),
    GEMINI('♊'),
    HAMMER_SICKLE('☭'),
    HEART('❤'),
    HEART_BULLET('❥'),
    HEART_EXCLAMATION('❣'),
    HEARTS('♥'),
    HEARTS_OUTLINE('♡'),
    HOT_SPRINGS('♨'),
    HOURGLASS('⌛'),
    JERUSALEM_CROSS('☩'),
    JUPITER('♃'),
    KEYBOARD('⌨'),
    KING('♚'),
    KING_OUTLINE('♔'),
    KNIGHT('♞'),
    KNIGHT_OUTLINE('♘'),
    LAST_QUARTER_MOON('☾'),
    LATIN_CROSS('✝'),
    LEFT('⬅'),
    LEFT_RIGHT('↔'),
    LEO('♌'),
    LIBRA('♎'),
    LIGHTNING('☇'),
    LORRAINE_CROSS('☨'),
    MALE('♂'),
    MULTIPLY('✖'),
    NATURAL('♮'),
    NEPTUNE('♆'),
    OPPOSITION('☍'),
    ORTHODOX_CROSS('☦'),
    PAWN('♟'),
    PAWN_OUTLINE('♙'),
    PEACE('☮'),
    PENCIL('✏'),
    PENCIL_DOWN_RIGHT('✎'),
    PENCIL_UP_RIGHT('✐'),
    PISCES('♓'),
    PLAY('▶'),
    PLUTO('♇'),
    POINT_DOWN_OUTLINE('☟'),
    POINT_LEFT('☚'),
    POINT_LEFT_OUTLINE('☜'),
    POINT_RIGHT('☛'),
    POINT_RIGHT_OUTLINE('☞'),
    POINT_UP('☝'),
    QUARTER_NOTE('♩'),
    QUEEN('♛'),
    QUEEN_OUTLINE('♕'),
    R('®'),
    RADIOACTIVE('☢'),
    REVERSE('◀'),
    RIGHT('➡'),
    ROOK('♜'),
    ROOK_OUTLINE('♖'),
    SAGITTARIUS('♐'),
    SALTIRE('☓'),
    SATURN('♄'),
    SCISSORS('✂'),
    SCORPIO('♏'),
    SECRET('㊙'),
    SHARP('♯'),
    SIXTEENTH_NOTES('♬'),
    SKULL('☠'),
    SMALL_SQUARE('▪'),
    SMALL_SQUARE_OUTLINES('▫'),
    SMILE('☺'),
    SMILE_FACE('☻'),
    SNOWFLAKE('❄'),
    SNOWMAN('☃'),
    SPADES('♠'),
    SPADES_OUTLINE('♤'),
    SPARKLE('❇'),
    STAR('★'),
    STAR_AND_CRESCENT('☪'),
    STAR_OF_DAVID('✡'),
    SUN('☀'),
    SUN_OUTLINE('☼'),
    SUN_SYMBOL('☉'),
    TAURUS('♉'),
    TELEPHONE('☎'),
    TELEPHONE_OUTLINE('☏'),
    THUNDERSTORM('☈'),
    TM('™'),
    TRIGRAM_EARTH('☷'),
    TRIGRAM_FIRE('☲'),
    TRIGRAM_HEAVEN('☰'),
    TRIGRAM_LAKE('☱'),
    TRIGRAM_MOUNTAIN('☶'),
    TRIGRAM_THUNDER('☳'),
    TRIGRAM_WATER('☵'),
    TRIGRAM_WIND('☴'),
    TURN_LEFT('↩'),
    TURN_RIGHT('↪'),
    UMBRELLA('☂'),
    UP('⬆'),
    UP_DOWN('↕'),
    UP_LEFT('↖'),
    UP_RIGHT('↗'),
    URANUS('♅'),
    V('✌'),
    VIRGO('♍'),
    WATCH('⌚'),
    WAVY_DASH('〰'),
    WHEEL_OF_DHARMA('☸'),
    WRITING_HAND('✍'),
    YIN_YANG('☯');

    private final char raw;

    Emoji(char raw) {
        this.raw = raw;
    }

    public char getRaw() {
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
