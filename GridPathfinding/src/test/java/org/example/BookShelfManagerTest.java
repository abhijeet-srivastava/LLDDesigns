package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookShelfManager")
class BookShelfManagerTest {

    private BookShelfManager shelf;

    @BeforeEach
    void setUp() {
        shelf = new BookShelfManager();
    }

    @Nested
    @DisplayName("addBooks")
    class AddBooks {

        @Test
        @DisplayName("inserts into an empty shelf")
        void insertsIntoEmptyShelf() {
            shelf.addBooks(0, List.of("A", "B", "C"));

            assertThat(shelf.getBooks()).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("inserts at the front, middle, and end")
        void insertsAtVariousPositions() {
            shelf.addBooks(0, List.of("B", "D"));
            shelf.addBooks(1, List.of("C"));
            shelf.addBooks(0, List.of("A"));
            shelf.addBooks(4, List.of("E"));

            assertThat(shelf.getBooks()).containsExactly("A", "B", "C", "D", "E");
        }

        @Test
        @DisplayName("keeps backward links intact after inserting before existing nodes")
        void keepsBackwardLinksIntact() {
            shelf.addBooks(0, List.of("A", "C"));
            shelf.addBooks(1, List.of("B"));

            shelf.setBookmarkIndex(2);
            assertThat(shelf.getBookMarkIndex()).isEqualTo(2);
            assertThat(shelf.getBooks()).containsExactly("A", "B", "C");
        }
    }

    @Nested
    @DisplayName("removeBooks")
    class RemoveBooks {

        @BeforeEach
        void seed() {
            shelf.addBooks(0, List.of("A", "B", "C", "D", "E"));
        }

        @Test
        @DisplayName("removes a middle range")
        void removesMiddleRange() {
            shelf.removeBooks(1, 3);

            assertThat(shelf.getBooks()).containsExactly("A", "D", "E");
        }

        @Test
        @DisplayName("removes through the end of the shelf without throwing")
        void removesThroughEnd() {
            shelf.removeBooks(3, 100);

            assertThat(shelf.getBooks()).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("removes from the front")
        void removesFromFront() {
            shelf.removeBooks(0, 2);

            assertThat(shelf.getBooks()).containsExactly("C", "D", "E");
        }

        @Test
        @DisplayName("still allows appending after removing the tail")
        void allowsAppendAfterRemovingTail() {
            shelf.removeBooks(3, 100);
            shelf.addBooks(3, List.of("F"));

            assertThat(shelf.getBooks()).containsExactly("A", "B", "C", "F");
        }
    }

    @Nested
    @DisplayName("moveBooks")
    class MoveBooks {

        @BeforeEach
        void seed() {
            shelf.addBooks(0, List.of("A", "B", "C", "D", "E"));
        }

        @Test
        @DisplayName("moves a segment later in the shelf")
        void movesSegmentForward() {
            // move "B" (index 1, size 1) to land at index 3 of the post-removal list
            shelf.moveBooks(1, 3, 1);

            assertThat(shelf.getBooks()).containsExactly("A", "C", "D", "B", "E");
        }

        @Test
        @DisplayName("moves a segment earlier in the shelf")
        void movesSegmentBackward() {
            // move "D" (index 3, size 1) to land at index 0
            shelf.moveBooks(3, 0, 2);

            assertThat(shelf.getBooks()).containsExactly("D", "E", "A", "B", "C");
        }

        @Test
        @DisplayName("moves a multi-book segment and preserves internal order")
        void movesMultiBookSegment() {
            // move "B","C" (index 1, size 2) to land at index 2 of the post-removal list
            shelf.moveBooks(1, 2, 2);

            assertThat(shelf.getBooks()).containsExactly("A", "D", "B", "C", "E");
        }

        @Test
        @DisplayName("moving a segment to the end drops no books")
        void movesSegmentToEnd() {
            shelf.moveBooks(0, 4, 1);

            assertThat(shelf.getBooks()).containsExactly("B", "C", "D", "E", "A");
        }

        @Test
        @DisplayName("no-op when fromIndex equals toIndex")
        void noOpWhenSameIndex() {
            shelf.moveBooks(2, 2, 1);

            assertThat(shelf.getBooks()).containsExactly("A", "B", "C", "D", "E");
        }

        @Test
        @DisplayName("keeps the list traversable end-to-end after a move")
        void keepsListConsistentAfterMove() {
            shelf.moveBooks(1, 3, 1);
            shelf.addBooks(5, List.of("F"));

            assertThat(shelf.getBooks()).containsExactly("A", "C", "D", "B", "E", "F");
        }
    }

    @Nested
    @DisplayName("getBooks")
    class GetBooks {

        @Test
        @DisplayName("returns an empty list, not null, for an empty shelf")
        void returnsEmptyListForEmptyShelf() {
            assertThat(shelf.getBooks()).isEmpty();
        }
    }

    @Nested
    @DisplayName("bookmark tracking")
    class Bookmark {

        @BeforeEach
        void seed() {
            shelf.addBooks(0, List.of("A", "B", "C"));
        }

        @Test
        @DisplayName("returns -1 when no bookmark has been set")
        void returnsMinusOneWhenUnset() {
            assertThat(shelf.getBookMarkIndex()).isEqualTo(-1);
        }

        @Test
        @DisplayName("round-trips the index it was set to, including index 0")
        void roundTripsIndex() {
            shelf.setBookmarkIndex(0);
            assertThat(shelf.getBookMarkIndex()).isEqualTo(0);

            shelf.setBookmarkIndex(2);
            assertThat(shelf.getBookMarkIndex()).isEqualTo(2);
        }

        @Test
        @DisplayName("setting an out-of-range index does not throw and clears the bookmark")
        void outOfRangeIndexClearsBookmark() {
            shelf.setBookmarkIndex(50);

            assertThat(shelf.getBookMarkIndex()).isEqualTo(-1);
        }

        @Test
        @DisplayName("returns -1 if the bookmarked book was since removed")
        void returnsMinusOneIfBookmarkedBookRemoved() {
            shelf.setBookmarkIndex(1);
            shelf.removeBooks(1, 2);

            assertThat(shelf.getBookMarkIndex()).isEqualTo(-1);
        }
    }
}