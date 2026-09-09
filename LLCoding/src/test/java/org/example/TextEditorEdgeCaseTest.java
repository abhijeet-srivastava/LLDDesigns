package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TextEditor edge cases")
public class TextEditorEdgeCaseTest {
    TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor();
    }

    @Test
    @DisplayName("addText at a row beyond the next available row is rejected")
    void addText_skippedRow_throws() {
        assertThatThrownBy(() -> editor.addText(3, 0, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("addText with negative row is rejected")
    void addText_negativeRow_throws() {
        assertThatThrownBy(() -> editor.addText(-1, 0, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("addText with negative column is rejected")
    void addText_negativeColumn_throws() {
        editor.addText(0, 0, "hello");
        assertThatThrownBy(() -> editor.addText(0, -1, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("addText with a column beyond the row length is rejected, including on a freshly created row")
    void addText_columnBeyondRowLength_throws() {
        assertThatThrownBy(() -> editor.addText(0, 5, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        editor.addText(0, 0, "hi");
        assertThatThrownBy(() -> editor.addText(0, 3, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("addText with empty text is a no-op that still records history")
    void addText_emptyText_isNoOp() {
        editor.addText(0, 0, "hello");
        editor.addText(0, 2, "");
        assertThat(editor.readLine(0)).isEqualTo("hello");

        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
    }

    @Test
    @DisplayName("deleteText on a row that does not exist is rejected")
    void deleteText_missingRow_throws() {
        assertThatThrownBy(() -> editor.deleteText(0, 0, 1))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("deleteText with negative start column is rejected")
    void deleteText_negativeStartColumn_throws() {
        editor.addText(0, 0, "hello");
        assertThatThrownBy(() -> editor.deleteText(0, -1, 1))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("deleteText with negative length is rejected")
    void deleteText_negativeLength_throws() {
        editor.addText(0, 0, "hello");
        assertThatThrownBy(() -> editor.deleteText(0, 0, -1))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("deleteText spanning past the end of the row is rejected")
    void deleteText_lengthBeyondRow_throws() {
        editor.addText(0, 0, "hi");
        assertThatThrownBy(() -> editor.deleteText(0, 0, 10))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("deleteText with zero length is a no-op that still records history")
    void deleteText_zeroLength_isNoOp() {
        editor.addText(0, 0, "hello");
        editor.deleteText(0, 2, 0);
        assertThat(editor.readLine(0)).isEqualTo("hello");

        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
    }

    @Test
    @DisplayName("deleteText can remove the entire row content")
    void deleteText_entireRow_leavesEmptyString() {
        editor.addText(0, 0, "hello");
        editor.deleteText(0, 0, 5);
        assertThat(editor.readLine(0)).isEqualTo("");
    }

    @Test
    @DisplayName("undo on an empty history is a no-op")
    void undo_emptyHistory_isNoOp() {
        editor.addText(0, 0, "hello");
        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("");

        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("");
    }

    @Test
    @DisplayName("redo on an empty redo history is a no-op")
    void redo_emptyHistory_isNoOp() {
        editor.addText(0, 0, "hello");
        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
    }

    @Test
    @DisplayName("a new edit after undo clears the redo history")
    void newEditAfterUndo_clearsRedoHistory() {
        editor.addText(0, 0, "hello");
        editor.undo();
        editor.addText(0, 0, "world");
        assertThat(editor.readLine(0)).isEqualTo("world");

        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("world");
    }

    @Test
    @DisplayName("readLine on a row that does not exist throws")
    void readLine_missingRow_throws() {
        assertThatThrownBy(() -> editor.readLine(0))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("readLine on a negative row throws")
    void readLine_negativeRow_throws() {
        editor.addText(0, 0, "hello");
        assertThatThrownBy(() -> editor.readLine(-1))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("multiple independent rows undo/redo without interfering with each other")
    void multipleRows_undoRedo_areIndependent() {
        editor.addText(0, 0, "hello");
        editor.addText(1, 0, "world");

        editor.undo();
        assertThat(editor.readLine(1)).isEqualTo("");
        assertThat(editor.readLine(0)).isEqualTo("hello");

        editor.undo();
        assertThat(editor.readLine(0)).isEqualTo("");

        editor.redo();
        editor.redo();
        assertThat(editor.readLine(0)).isEqualTo("hello");
        assertThat(editor.readLine(1)).isEqualTo("world");
    }
}