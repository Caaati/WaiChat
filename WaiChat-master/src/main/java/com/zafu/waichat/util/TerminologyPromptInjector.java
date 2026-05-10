package com.zafu.waichat.util;

/**
 * 将命中的术语表拼入 system prompt。
 */
public final class TerminologyPromptInjector {

    private TerminologyPromptInjector() {
    }

    public static String mergeSystemPrompt(String baseSys, String glossaryBlock) {
        if (glossaryBlock == null || glossaryBlock.isBlank()) {
            return baseSys;
        }
        return baseSys + "\n\n【术语表】以下为当前上下文中命中的术语，生成时请优先采用下列释义，不要编造未给出的术语定义。\n"
                + glossaryBlock;
    }
}
