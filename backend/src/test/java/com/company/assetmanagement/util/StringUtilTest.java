package com.company.assetmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StringUtil Tests")
class StringUtilTest {
    
    @Test
    @DisplayName("Should check if string is blank")
    void shouldCheckIfBlank() {
        assertThat(StringUtil.isBlank(null)).isTrue();
        assertThat(StringUtil.isBlank("")).isTrue();
        assertThat(StringUtil.isBlank("   ")).isTrue();
        assertThat(StringUtil.isBlank("test")).isFalse();
    }
    
    @Test
    @DisplayName("Should check if string is not blank")
    void shouldCheckIfNotBlank() {
        assertThat(StringUtil.isNotBlank("test")).isTrue();
        assertThat(StringUtil.isNotBlank(null)).isFalse();
        assertThat(StringUtil.isNotBlank("")).isFalse();
        assertThat(StringUtil.isNotBlank("   ")).isFalse();
    }
    
    @Test
    @DisplayName("Should trim to null")
    void shouldTrimToNull() {
        assertThat(StringUtil.trimToNull("  test  ")).isEqualTo("test");
        assertThat(StringUtil.trimToNull("   ")).isNull();
        assertThat(StringUtil.trimToNull("")).isNull();
        assertThat(StringUtil.trimToNull(null)).isNull();
    }
    
    @Test
    @DisplayName("Should trim to empty")
    void shouldTrimToEmpty() {
        assertThat(StringUtil.trimToEmpty("  test  ")).isEqualTo("test");
        assertThat(StringUtil.trimToEmpty("   ")).isEmpty();
        assertThat(StringUtil.trimToEmpty(null)).isEmpty();
    }
    
    @Test
    @DisplayName("Should capitalize first letter")
    void shouldCapitalizeFirstLetter() {
        assertThat(StringUtil.capitalize("test")).isEqualTo("Test");
        assertThat(StringUtil.capitalize("TEST")).isEqualTo("TEST");
        assertThat(StringUtil.capitalize("t")).isEqualTo("T");
        assertThat(StringUtil.capitalize("")).isEqualTo("");
        assertThat(StringUtil.capitalize(null)).isNull();
    }
    
    @Test
    @DisplayName("Should convert to camelCase")
    void shouldConvertToCamelCase() {
        assertThat(StringUtil.toCamelCase("hello world")).isEqualTo("helloWorld");
        assertThat(StringUtil.toCamelCase("hello_world")).isEqualTo("helloWorld");
        assertThat(StringUtil.toCamelCase("hello-world")).isEqualTo("helloWorld");
        assertThat(StringUtil.toCamelCase("HELLO WORLD")).isEqualTo("helloWorld");
    }
    
    @Test
    @DisplayName("Should convert to snake_case")
    void shouldConvertToSnakeCase() {
        assertThat(StringUtil.toSnakeCase("helloWorld")).isEqualTo("hello_world");
        assertThat(StringUtil.toSnakeCase("HelloWorld")).isEqualTo("hello_world");
        assertThat(StringUtil.toSnakeCase("hello world")).isEqualTo("hello_world");
        assertThat(StringUtil.toSnakeCase("hello-world")).isEqualTo("hello_world");
    }
    
    @Test
    @DisplayName("Should convert to kebab-case")
    void shouldConvertToKebabCase() {
        assertThat(StringUtil.toKebabCase("helloWorld")).isEqualTo("hello-world");
        assertThat(StringUtil.toKebabCase("HelloWorld")).isEqualTo("hello-world");
        assertThat(StringUtil.toKebabCase("hello world")).isEqualTo("hello-world");
        assertThat(StringUtil.toKebabCase("hello_world")).isEqualTo("hello-world");
    }
    
    @Test
    @DisplayName("Should truncate string")
    void shouldTruncateString() {
        assertThat(StringUtil.truncate("hello world", 5)).isEqualTo("hello");
        assertThat(StringUtil.truncate("hello", 10)).isEqualTo("hello");
        assertThat(StringUtil.truncate(null, 5)).isNull();
    }
    
    @Test
    @DisplayName("Should truncate string with ellipsis")
    void shouldTruncateWithEllipsis() {
        assertThat(StringUtil.truncateWithEllipsis("hello world", 8)).isEqualTo("hello...");
        assertThat(StringUtil.truncateWithEllipsis("hello", 10)).isEqualTo("hello");
        assertThat(StringUtil.truncateWithEllipsis("hello world", 2)).isEqualTo("he");
    }
    
    @Test
    @DisplayName("Should join strings")
    void shouldJoinStrings() {
        List<String> values = List.of("hello", "world", "test");
        assertThat(StringUtil.join(values, ", ")).isEqualTo("hello, world, test");
        assertThat(StringUtil.join(List.of(), ", ")).isEmpty();
        assertThat(StringUtil.join(null, ", ")).isEmpty();
    }
    
    @Test
    @DisplayName("Should split and trim strings")
    void shouldSplitAndTrim() {
        List<String> result = StringUtil.splitAndTrim("hello, world, test", ",");
        assertThat(result).containsExactly("hello", "world", "test");
        
        result = StringUtil.splitAndTrim("hello,  world  , test", ",");
        assertThat(result).containsExactly("hello", "world", "test");
        
        assertThat(StringUtil.splitAndTrim(null, ",")).isEmpty();
        assertThat(StringUtil.splitAndTrim("", ",")).isEmpty();
    }
    
    @Test
    @DisplayName("Should mask string")
    void shouldMaskString() {
        assertThat(StringUtil.mask("password123", 3)).isEqualTo("********123");
        assertThat(StringUtil.mask("test", 2)).isEqualTo("**st");
        assertThat(StringUtil.mask("abc", 5)).isEqualTo("***");
        assertThat(StringUtil.mask(null, 3)).isNull();
    }
    
    @Test
    @DisplayName("Should remove whitespace")
    void shouldRemoveWhitespace() {
        assertThat(StringUtil.removeWhitespace("hello world")).isEqualTo("helloworld");
        assertThat(StringUtil.removeWhitespace("  test  ")).isEqualTo("test");
        assertThat(StringUtil.removeWhitespace(null)).isNull();
    }
    
    @Test
    @DisplayName("Should check if string is numeric")
    void shouldCheckIfNumeric() {
        assertThat(StringUtil.isNumeric("123")).isTrue();
        assertThat(StringUtil.isNumeric("0")).isTrue();
        assertThat(StringUtil.isNumeric("abc")).isFalse();
        assertThat(StringUtil.isNumeric("12.3")).isFalse();
        assertThat(StringUtil.isNumeric(null)).isFalse();
        assertThat(StringUtil.isNumeric("")).isFalse();
    }
    
    @Test
    @DisplayName("Should pad string left")
    void shouldPadLeft() {
        assertThat(StringUtil.padLeft("123", 5, '0')).isEqualTo("00123");
        assertThat(StringUtil.padLeft("test", 6, ' ')).isEqualTo("  test");
        assertThat(StringUtil.padLeft("hello", 3, '0')).isEqualTo("hello");
        assertThat(StringUtil.padLeft(null, 5, '0')).isEqualTo("00000");
    }
    
    @Test
    @DisplayName("Should pad string right")
    void shouldPadRight() {
        assertThat(StringUtil.padRight("123", 5, '0')).isEqualTo("12300");
        assertThat(StringUtil.padRight("test", 6, ' ')).isEqualTo("test  ");
        assertThat(StringUtil.padRight("hello", 3, '0')).isEqualTo("hello");
        assertThat(StringUtil.padRight(null, 5, '0')).isEqualTo("00000");
    }
}
