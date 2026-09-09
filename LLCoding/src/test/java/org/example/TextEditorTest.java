package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextEditor")
public class TextEditorTest {
    TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor();
    }
    @Test
    @DisplayName("Validate countPrefixOccurrences")
    void testCountPrefixOccurrences() {
        editor.addText(0, 0, "hello");
        assertThat(editor.readLine(0)).isEqualTo("hello");
        editor.addText(1, 0, "world");
        assertThat(editor.readLine(1)).isEqualTo("world");
        editor.addText(0, 5, "-there");
        assertThat(editor.readLine(0)).isEqualTo("hello-there");
        editor.deleteText(0, 5, 6);
        assertThat(editor.readLine(0)).isEqualTo("hello");
        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("hello-there");
        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
        editor.addText(1, 5, "-wide web");
        assertThat(editor.readLine(1)).isEqualTo("world-wide web");
        editor.deleteText(1, 5, 5);
        assertThat(editor.readLine(1)).isEqualTo("world web");
        editor.undo();
        assertThat(editor.readLine(1)).isEqualTo("world-wide web");

        assertThat(editor.readLine(0)).isEqualTo("hello");
        editor.addText(0, 5, "!");
        assertThat(editor.readLine(0)).isEqualTo("hello!");
        editor.addText(0, 6, "!");
        assertThat(editor.readLine(0)).isEqualTo("hello!!");
        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("hello!");
        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("hello!");
        editor.addText(0, 6, "?");
        assertThat(editor.readLine(0)).isEqualTo("hello!?");
        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("hello!?");
        editor.addText(2, 0, "aa bb-cc");
        assertThat(editor.readLine(2)).isEqualTo("aa bb-cc");
        editor.deleteText(2, 0, 8);
        assertThat(editor.readLine(2)).isEqualTo("");
        editor.undo();
        assertThat(editor.readLine(2)).isEqualTo("aa bb-cc");

        editor.addText(3, 0, "world");
        assertThat(editor.readLine(3)).isEqualTo("world");
        editor.addText(3, 0, "hello-");
        assertThat(editor.readLine(3)).isEqualTo("hello-world");

    }
}
