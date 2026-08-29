package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TextEditor {

    private List<String> editor;

    private Deque<Command> undoStack;
    private Deque<Command> redoStack;
    public TextEditor() {
        editor = new ArrayList<>();
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
    }

    public void addText(int row, int column, String text) {
        if(row < 0 || row > editor.size()) {
            throw new IndexOutOfBoundsException("Row " + row + " does not exist; next available row is " + editor.size());
        }
        if(row == editor.size()) {
            editor.add("");
        }
        String original = editor.get(row);
        if(column < 0 || column > original.length()) {
            throw new IndexOutOfBoundsException("Column " + column + " out of bounds for row of length " + original.length());
        }
        String modified = original.substring(0, column) + text + original.substring(column);
        editor.set(row, modified);
        undoStack.push(new Command(row, column, text, CommandType.ADD_TEXT));
        redoStack.clear();
    }

    public void deleteText(int row, int startColumn, int length) {
        if(row < 0 || row >= editor.size()) {
            throw new IndexOutOfBoundsException("Row " + row + " does not exist");
        }
        String original = editor.get(row);
        if(startColumn < 0 || length < 0 || startColumn + length > original.length()) {
            throw new IndexOutOfBoundsException("Range [" + startColumn + ", " + (startColumn + length) + ") out of bounds for row of length " + original.length());
        }
        String deleted = original.substring(startColumn, startColumn + length);
        String modified = original.substring(0, startColumn) + original.substring(startColumn+length);
        editor.set(row, modified);
        undoStack.push(new Command(row, startColumn, deleted, CommandType.DELETE_TEXT));
        redoStack.clear();
    }

    public void undo() {
        if(undoStack.isEmpty()) {
            return;
        }
        Command command = undoStack.pop();
        String original = editor.get(command.row);
        if(command.type == CommandType.ADD_TEXT) {
            String modified = original.substring(0, command.col) + original.substring(command.col+command.text.length());
            editor.set(command.row, modified);
        } else {
            String modified = original.substring(0, command.col) + command.text + original.substring(command.col);
            editor.set(command.row, modified);
        }
        redoStack.push(command);
    }

    public void redo() {
        if(redoStack.isEmpty()) {
            return;
        }
        Command command = redoStack.pop();
        String original = editor.get(command.row);
        if(command.type == CommandType.ADD_TEXT) {
            String modified = original.substring(0, command.col) + command.text + original.substring(command.col);
            editor.set(command.row, modified);
        } else {
            String modified = original.substring(0, command.col) + original.substring(command.col+command.text.length());
            editor.set(command.row, modified);
        }
        undoStack.push(command);
    }
    public String readLine(int row) {
        return editor.get(row);
    }

    private class Command {
        int row;
        int col;
        String text;
        CommandType type;

        public Command(int row, int col, String text, CommandType type) {
            this.row = row;
            this.col = col;
            this.text = text;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Command{" +
                    "row=" + row +
                    ", col=" + col +
                    ", text='" + text + '\'' +
                    ", type=" + type +
                    '}';
        }
    }

    private enum CommandType {
        ADD_TEXT,
        DELETE_TEXT;
    }
}
