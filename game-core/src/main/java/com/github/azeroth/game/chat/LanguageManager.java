package com.github.azeroth.game.chat;


import com.github.azeroth.game.LanguageDesc;

import java.util.ArrayList;


public class LanguageManager {
    private static final int[] SHashtable = {0x486E26EE, (int) 0xDCAA16B3, (int) 0xE1918EEF, 0x202DAFDB, 0x341C7DC7, 0x1C365303, 0x40EF2D37, 0x65FD5E49, (int) 0xD6057177, (int) 0x904ECE93, 0x1C38024F, (int) 0x98FD323B, (int) 0xE3061AE7, (int) 0xA39B0FA1, (int) 0x9797F25F, (int) 0xE4444563};

    private final MultiMap<Integer, LanguageDesc> langsMap = new MultiMap<Integer, LanguageDesc>();
    private final MultiMap<Tuple<Integer, Byte>, String> wordsMap = new MultiMap<Tuple<Integer, Byte>, String>();

    private LanguageManager() {
    }

    private static char upper_backslash(char c) {
        return c == '/' ? '\\' : Character.toUpperCase(c);
    }

    public final void loadSpellEffectLanguage(SpellEffectRecord spellEffect) {
        var languageId = (int) spellEffect.EffectMiscValue[0];
        langsMap.add(languageId, new LanguageDesc(spellEffect.spellID, 0)); // register without a skill id for now
    }

    public final void loadLanguages() {
        var oldMSTime = System.currentTimeMillis();

        // Load languages from Languages.db2. Just the id, we don't need the name
        for (var langEntry : CliDB.LanguagesStorage.values()) {
            var spellsRange = langsMap.get(langEntry.id);

            if (spellsRange.isEmpty()) {
                langsMap.add(langEntry.id, new LanguageDesc());
            } else {
                ArrayList<LanguageDesc> langsWithSkill = new ArrayList<>();

                for (var spellItr : spellsRange) {
                    for (var skillPair : global.getSpellMgr().getSkillLineAbilityMapBounds(spellItr.spellId)) {
                        langsWithSkill.add(new LanguageDesc(spellItr.spellId, (int) skillPair.skillLine));
                    }
                }

                for (var langDesc : langsWithSkill) {
                    // erase temporary assignment that lacked skill
                    langsMap.remove(langEntry.id, new LanguageDesc(langDesc.spellId, 0));
                    langsMap.add(langEntry.id, langDesc);
                }
            }
        }

        // Add the languages used in code in case they don't exist
        langsMap.add((int) language.Universal.getValue(), new LanguageDesc());
        langsMap.add((int) language.Addon.getValue(), new LanguageDesc());
        langsMap.add((int) language.AddonLogged.getValue(), new LanguageDesc());

        // Log load time
        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s languages in %2$s ms", langsMap.count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void loadLanguagesWords() {
        var oldMSTime = System.currentTimeMillis();

        int wordsNum = 0;

        for (var wordEntry : CliDB.LanguageWordsStorage.values()) {
            var length = (byte) Math.min(18, wordEntry.Word.length);

            var key = Tuple.create(wordEntry.LanguageID, length);

            wordsMap.add(key, wordEntry.Word);
            ++wordsNum;
        }

        // log load time
        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s word groups from %2$s words in %3$s ms", wordsMap.count, wordsNum, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final String translate(String msg, int language, Locale locale) {
        var textToTranslate = "";
        tangible.RefObject<String> tempRef_textToTranslate = new tangible.RefObject<String>(textToTranslate);
        stripHyperlinks(msg, tempRef_textToTranslate);
        textToTranslate = tempRef_textToTranslate.refArgValue;
        tangible.RefObject<String> tempRef_textToTranslate2 = new tangible.RefObject<String>(textToTranslate);
        replaceUntranslatableCharactersWithSpace(tempRef_textToTranslate2);
        textToTranslate = tempRef_textToTranslate2.refArgValue;

        var result = "";
        LocalizedString tokens = new LocalizedString();

        for (String str : tokens) {
            var wordLen = (int) Math.min(18, str.length());
            var wordGroup = findWordGroup(language, wordLen);

            if (!wordGroup.isEmpty()) {
                var wordHash = SStrHash(str, true);
                var idxInsideGroup = (byte) (wordHash % wordGroup.size());

                var replacementWord = wordGroup.get(idxInsideGroup);

                switch (locale) {
                    case koKR:
                    case zhCN:
                    case zhTW: {
                        var length = Math.min(str.length(), replacementWord.length());

                        for (var i = 0; i < length; ++i) {
                            if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') {
                                result += Character.toUpperCase(replacementWord.charAt(i));
                            } else {
                                result += replacementWord.charAt(i);
                            }
                        }

                        break;
                    }
                    default: {
                        var length = Math.min(str.length(), replacementWord.length());

                        for (var i = 0; i < length; ++i) {
                            if (Character.isUpperCase(str.charAt(i))) {
                                result += Character.toUpperCase(replacementWord.charAt(i));
                            } else {
                                result += Character.toLowerCase(replacementWord.charAt(i));
                            }
                        }

                        break;
                    }
                }
            }

            result += ' ';
        }

        if (!result.isEmpty()) {
            tangible.StringHelper.remove(result, result.length() - 1);
        }

        return result;
    }

    public final boolean isLanguageExist(Language languageId) {
        return CliDB.LanguagesStorage.HasRecord((int) languageId.getValue());
    }

    public final ArrayList<LanguageDesc> getLanguageDescById(Language languageId) {
        return langsMap.get((int) languageId.getValue());
    }

    public final boolean forEachLanguage(tangible.Func2Param<Integer, LanguageDesc, Boolean> callback) {
        for (var pair : langsMap.KeyValueList) {
            if (!callback.invoke(pair.key, pair.value)) {
                return false;
            }
        }

        return true;
    }

    private ArrayList<String> findWordGroup(int language, int wordLen) {
        return wordsMap.get(Tuple.create(language, (byte) wordLen));
    }

    private void stripHyperlinks(String source, tangible.RefObject<String> dest) {
        var destChar = new char[source.length()];

        var destSize = 0;
        var skipSquareBrackets = false;

        for (var i = 0; i < source.length(); ++i) {
            var c = source.charAt(i);

            if (c != '|') {
                if (!skipSquareBrackets || (c != '[' && c != ']')) {
                    destChar[destSize++] = source.charAt(i);
                }

                continue;
            }

            if (i + 1 >= source.length()) {
                break;
            }

            switch (source.charAt(i + 1)) {
                case 'c':
                case 'C':
                    // skip color
                    i += 9;

                    break;
                case 'r':
                    ++i;

                    break;
                case 'H':
                    // skip just past first |h
                    i = source.indexOf("|h", i);

                    if (i != -1) {
                        i += 2;
                    }

                    skipSquareBrackets = true;

                    break;
                case 'h':
                    ++i;
                    skipSquareBrackets = false;

                    break;
                case 'T':
                    // skip just past closing |t
                    i = source.indexOf("|t", i);

                    if (i != -1) {
                        i += 2;
                    }

                    break;
                default:
                    break;
            }
        }

        dest.refArgValue = new String(destChar, 0, destSize);
    }

    private void replaceUntranslatableCharactersWithSpace(tangible.RefObject<String> text) {
        var chars = text.refArgValue.toCharArray();

        for (var i = 0; i < text.refArgValue.length(); ++i) {
            var w = chars[i];

            if (!Extensions.isExtendedLatinCharacter(w) && !Character.IsNumber(w) && w <= 0xFF && w != '\\') {
                chars[i] = ' ';
            }
        }

        text.refArgValue = new String(chars);
    }

    private int SStrHash(String str, boolean caseInsensitive) {
        return SStrHash(str, caseInsensitive, 0x7FED7FED);
    }

    private int SStrHash(String str, boolean caseInsensitive, int seed) {
        var shift = (int) 0xEEEEEEEE;

        for (var i = 0; i < str.length(); ++i) {
            var c = str.charAt(i);

            if (caseInsensitive) {
                c = upper_backslash(c);
            }

            seed = (SHashtable[c >> 4] - SHashtable[c & 0xF]) ^ (shift + seed);
            shift = c + seed + 33 * shift + 3;
        }

        return seed != 0 ? seed : 1;
    }
}
