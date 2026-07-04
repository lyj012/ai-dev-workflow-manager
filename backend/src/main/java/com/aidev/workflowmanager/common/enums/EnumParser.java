package com.aidev.workflowmanager.common.enums;

public final class EnumParser {

    private EnumParser() {
    }

    public static <E extends Enum<E> & CodeEnum> E parseByCode(Class<E> enumType, String code) {
        if (code == null) {
            return null;
        }
        for (E item : enumType.getEnumConstants()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid " + enumType.getSimpleName() + ": " + code);
    }
}
