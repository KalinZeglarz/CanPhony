package pl.poznan.put.structures

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject

import java.lang.reflect.Field

@Slf4j
@ToString
class PasswordPolicy implements JSONable {

    public int minPasswordLength = 0
    public int numberOfUppercaseCharacters = 0
    public int numberOfLowercaseCharacters = 0
    public int numberOfNumericCharacters = 0
    public int numberOfSpecialCharacters = 0
    public String specialCharacters = ""

    boolean validatePassword(String password) {
        boolean result = true
        result &= password.length() >= minPasswordLength
        result &= password.findAll(~'[A-Z]').size() >= numberOfUppercaseCharacters
        result &= password.findAll(~'[a-z]').size() >= numberOfLowercaseCharacters
        result &= password.findAll(~'[0-9]').size() >= numberOfNumericCharacters
        if (!specialCharacters.isEmpty()) {
            result &= password.findAll(~"[${specialCharacters}]").size() >= numberOfSpecialCharacters
        }
        return result
    }

    void setField(String fieldName, String fieldValue) {
        for (Field field in PasswordPolicy.getDeclaredFields()) {
            if (field.getName() == fieldName) {
                if (field.getType() == String) {
                    field.set(this, fieldValue)
                } else if (field.getType() == int) {
                    field.setInt(this, Integer.valueOf(fieldValue))
                } else {
                    log.error("variable type not handled: ${fieldName}")
                    throw new RuntimeException("variable type not handled: ${fieldName}")
                }
                return
            }
        }
    }

    Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>()
        for (Field field in PasswordPolicy.getFields()) {
            if (field.getName().contains('$')) {
                continue
            }
            result.put(field.getName(), field.get(this).toString())
        }
        return result
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject(toMap())
    }

    Map<String, String> toPrettyMap() {
        Map<String, String> result = new TreeMap<>()
        for (Map.Entry<String, String> field in this.toMap()) {
            result.put(deCamelCase(field.getKey()), field.getValue())
        }
        return result
    }

    private static String deCamelCase(String fieldName) {
        String result = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(fieldName), StringUtils.SPACE)
        result = result.toLowerCase()
        return new StringBuilder(result).replace(0, 1, result[0].toUpperCase()).toString()
    }

    static PasswordPolicy parseJSON(String json) {
        Map<String, String> fieldMap = new JSONObject(json).toMap() as Map<String, String>
        PasswordPolicy result = new PasswordPolicy()
        for (Map.Entry<String, String> field in fieldMap) {
            result.setField(field.getKey(), field.getValue())
        }
        return result
    }

}
