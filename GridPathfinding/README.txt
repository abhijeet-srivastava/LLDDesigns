GridPathfinding
===============

Grid path-finding algorithms over an m x n `heights` matrix, from (0,0) to
(m-1, n-1), moving only up/down/left/right.

Let V = m*n (cells) and E ~ 2V (grid edges: one right-neighbor + one
down-neighbor per cell).


1. minimumElevationGain
------------------------
Problem: minimize the total elevation GAIN along the path, where moving
from a lower cell to a higher cell costs (higher - lower), and moving
downhill costs 0. This is an additive (sum) shortest-path problem with
directional (asymmetric) edge costs.

High-level flow:
  - Dijkstra's algorithm, relaxing by SUM instead of by max.
  - gainTo[r][c] tracks the cheapest total uphill cost found so far to
    reach (r, c); starts at MAX_VALUE except gainTo[0][0] = 0.
  - A min-heap pops the cell with the smallest accumulated gain so far.
  - On pop: skip if already visited (stale heap entry); mark visited;
    return immediately if this is the destination.
  - For each unvisited neighbor: step cost = max(0, height[neighbor] -
    height[current]); if the new total gain improves gainTo[neighbor],
    update it and push the neighbor onto the heap.

Time complexity:  O(E log V)  ~  O(V log V)
Space complexity: O(V)  (gainTo, visited, heap entries)


2. minimumEffortPathDijkstra
-----------------------------
Problem: minimize the MAXIMUM absolute height difference between adjacent
cells along the path (a "bottleneck"/minimax path problem), not a sum.

High-level flow:
  - Dijkstra's algorithm, relaxing by MAX instead of by sum.
  - effortTo[r][c] tracks the smallest possible "path bottleneck" (max
    step seen so far) to reach (r, c); starts at maxHeight - minHeight
    for all cells except effortTo[0][0] = 0.
  - A min-heap pops the cell whose current path-bottleneck is smallest.
  - On pop: skip if already visited (stale heap entry); mark visited;
    return immediately if this is the destination.
  - For each unvisited neighbor: candidate bottleneck = max(current
    path's bottleneck, abs(height[current] - height[neighbor])); if it
    improves effortTo[neighbor], update it and push the neighbor.

Time complexity:  O(E log V)  ~  O(V log V)
Space complexity: O(V)  (effortTo, visited, heap entries)


3. minimumEffortPathDfs
-------------------------
Problem: same minimax objective as above, solved independently via binary
search over the answer plus a feasibility check.

High-level flow:
  - Binary search over candidate effort values in
    [0, maxHeight - minHeight].
  - Feasibility check for a candidate `mid`: run a DFS from (0,0) that is
    only allowed to step onto a neighbor if abs(height diff) <= mid, and
    that permanently marks each cell visited once explored (no
    backtracking/unmarking). This is correct because "is there a path
    whose bottleneck is <= mid" reduces to a static connectivity question
    on the fixed subgraph of edges with weight <= mid — reachability
    here does not depend on which route was taken, so a single DFS pass
    per candidate is sufficient.
  - If feasible, record `mid` as a candidate answer and search lower
    (r = mid - 1); otherwise search higher (l = mid + 1).

Time complexity:  O(V log(range))   where range = maxHeight - minHeight
Space complexity: O(V)  (visited array + recursion stack)


4. minimumEffortPath
----------------------
Problem: same minimax objective, solved independently via Kruskal's
MST / Union-Find (minimum bottleneck path).

High-level flow:
  - Build the full edge list for the grid graph: one edge per pair of
    vertically or horizontally adjacent cells, weighted by the absolute
    height difference.
  - Sort all edges ascending by weight.
  - Union cells via Union-Find (path compression + union by rank) in
    that ascending order, exactly as in Kruskal's MST algorithm.
  - After each union, check whether the source (index 0) and destination
    (index m*n - 1) are now in the same component. The first edge weight
    at which this becomes true is the answer — this works because the
    unique path between any two nodes in a graph's MST is guaranteed to
    be the minimum-bottleneck path between them.

Time complexity:  O(E log E)  ~  O(V log V)   (dominated by the sort)
Space complexity: O(V)  (edge list, union-find parent/rank arrays)


5. findPath
-------------
Problem: given grid dimensions m x n, a start cell and a target cell,
find the MINIMUM-HOP path between them (fewest number of moves), moving
only up/down/left/right, and return it formatted as
"(i, j) -> (i+1, j) -> ... -> (p, q)".

High-level flow:
  - Plain BFS from start, since the grid is unweighted (every move costs
    the same) — BFS explores cells level by level, so the first time the
    target is dequeued, it has necessarily been reached via the fewest
    possible hops.
  - prevRow[r][c] / prevCol[r][c] record, for every discovered cell, the
    cell it was first reached from — used to reconstruct the path.
  - visited[][] prevents re-enqueueing a cell once discovered.
  - Once the target is dequeued (or found unreachable), walk backwards
    from target to start via the prev pointers, reverse the resulting
    list, and join it into the "(i, j) -> ..." string format.
  - start == target is a special case, returning just "(i, j)" with no
    arrow.

Why not DFS: DFS explores depth-first, so the first path it finds to the
target is whichever branch happens to be explored first — there is no
guarantee it has the fewest hops. Making DFS hop-optimal would require
exploring all simple paths and keeping the shortest (no valid pruning
exists, since a cell reached in few hops via one branch might be
revisited in many hops via another), which is O(4^V) in the worst case
instead of BFS's O(V + E).

Time complexity:  O(V + E)  ~  O(V)
Space complexity: O(V)  (visited, prevRow/prevCol, queue, reconstructed path)


6. maximumSafenessFactor
--------------------------
Problem: given grid dimensions m x n, a start cell, a target cell, and a
list of cells containing cats, find the path from start to target that
MAXIMIZES the MINIMUM distance to any cat along the path (the mouse's
"safeness factor"). This is a minimax path problem again, but the edge
weights are derived from a separate multi-source shortest-path
computation rather than given directly.

High-level flow:
  - Multi-source BFS seeded from every cat simultaneously computes
    safeness[r][c] = shortest-path distance from (r, c) to the nearest
    cat. All cells are initialized to a sentinel of m*n (larger than any
    real distance) before the BFS relaxes them downward; cat cells start
    at distance 0. (If start or target itself holds a cat, the answer is
    trivially 0.)
  - Binary search over candidate safeness thresholds in [0, m*n]. For a
    candidate `mid`, feasibility is checked by `existsPath`: both start
    and target must individually have safeness >= mid, and then a DFS
    (permanently marking cells visited, same static-connectivity
    reasoning as minimumEffortPathDfs/#3) checks whether start and
    target are connected using only cells with safeness >= mid.
  - If feasible, record `mid` as a candidate answer and search higher
    (l = mid + 1); otherwise search lower (r = mid - 1).

Time complexity:  O(V + E) for the multi-source BFS, plus
                   O(V log V) for the binary search (each of the
                   O(log V) iterations runs an O(V) DFS)
                   ~  O(V log V) overall
Space complexity: O(V)  (safeness grid, visited array, BFS/DFS queues)


7. canReachDestinationBFS
----------------------------
Problem: given a list of routers ("id,x,y") on a 2D plane, a source router,
a destination router, and a broadcast range, determine whether a message
can reach the destination without colliding. Each router broadcasts once,
to every OTHER router within Euclidean `range`; if two or more messages
arrive at the same router at the same earliest time (within
ARRIVAL_TIME_TOLERANCE = 0.1), they collide and that router never
broadcasts. Here V = number of routers (not grid cells), and edges are a
proximity graph, not the fixed grid adjacency used in #1-6.

High-level flow:
  - Build a proximity graph: for every pair of routers, add an undirected
    edge if their Euclidean distance <= range. Unlike the grid problems,
    this graph is not fixed-degree — in the worst case (large range) it
    is complete, giving E = O(V^2) instead of O(V).
  - Run a Dijkstra-style event simulation seeded from the source's
    neighbors (arrival time = distance from source).
  - Pop the earliest-arriving event. Drain the priority queue for any
    other events tied to the SAME target within ARRIVAL_TIME_TOLERANCE
    (put non-matching ties back). If more than one event ties for that
    target's earliest arrival, it's a collision: the target is marked
    settled but does NOT broadcast further.
  - If the popped target is the destination, the answer is
    "exactly one event arrived" (no collision at the destination).
  - Otherwise, if exactly one event arrived (no collision), that router
    broadcasts: push a new event to each of its unvisited neighbors, with
    arrival time = current time + distance to neighbor.
  - `settled` prevents a router from being processed more than once,
    since arrival times only increase (Dijkstra invariant) — the first
    time a router is settled is guaranteed to be at its true earliest
    arrival time.

Time complexity:
  - Graph build: O(V^2)   (checks every pair of routers)
  - Event propagation: O(E log V), and since E = O(V^2) here, this is
    O(V^2 log V) in the worst case (dense proximity graph)
  - Overall: O(V^2) + O(V^2 log V)  ~  O(V^2 log V)
Space complexity: O(V^2)  (adjacency lists in the worst case, dominates
                   the O(V) router map / priority queue / settled set)


8. TowerReachable.calculate
------------------------------
Problem: given a string of the form "expression = math_expr" (math_expr
containing digits, +, -, *, /, and parentheses, always well-formed),
evaluate it following standard operator precedence (BODMAS), returning
an int.

High-level flow: a recursive-descent parser first builds an expression
tree, which is then evaluated recursively.

  Grammar (highest level to lowest, mirroring precedence):
    expression := term (('+' | '-') term)*      // lowest precedence
    term       := factor (('*' | '/') factor)*   // higher precedence
    factor     := ['-'|'+'] ( number | '(' expression ')' )

  - A single mutable int[] pos cursor is threaded through parseExpression /
    parseTerm / parseFactor (Java strings can't be advanced by reference
    otherwise), tracking the current read position across all calls.
  - Each level calls into the level below it first. This is what makes
    '*'/'/' bind tighter than '+'/'-': by the time parseExpression sees a
    '+', parseTerm has already fully consumed any '*'/'/' chain around it.
    Example, "2 + 3 * 4": parseExpression gets leaf Node(2) from
    parseTerm, sees '+', then calls parseTerm again for the right side —
    that call sees 3, then '*', and loops internally to build
    Node("*", 3, 4) before returning. parseExpression then combines them
    into Node("+", 2, Node("*", 3, 4)), giving the correct 2 + (3*4) = 14
    via tree structure rather than precedence numbers.
  - The while loops in parseExpression/parseTerm keep wrapping the
    accumulated node as the new left child, giving left-associativity
    (e.g. "1 - 2 - 3" builds Node("-", Node("-", 1, 2), 3), not the
    wrong right-associative reading).
  - parseFactor handles '(' by recursing back to the top of the grammar
    (parseExpression) and consuming the matching ')' — this lets
    parentheses override default precedence, since the inner expression
    is fully resolved into one subtree before the caller's '*'/'/' loop
    ever sees it.
  - parseFactor also peeks for a leading unary '-'/'+' before a number or
    '('; a unary minus is rewritten as Node("-", Node(0), innerNode),
    reusing the same binary apply() logic instead of a separate unary
    code path.
  - Leaves are built by scanning a contiguous run of digits into an int
    (operator == null on a Node marks it as a leaf holding `value`).
  - skipWhitespace runs at the start of each level so spaces between
    tokens are ignored anywhere.
  - Node.evaluate() walks the tree post-order: leaves return their value
    directly; internal nodes recursively evaluate left/right then apply()
    the operator (+, -, *, /, with / truncating toward zero as Java's
    native int division does).

Time complexity:  O(n)   (single left-to-right scan of the expression,
                   n = expression length)
Space complexity: O(n)   (expression tree + recursion stack, bounded by
                   nesting depth / number of tokens)


9. StackExpressionEvaluator.calculate
----------------------------------------
Problem: same as #8 (TowerReachable.calculate) — evaluate a well-formed
"expression = math_expr" string following BODMAS — but solved with a
single-pass iterative two-stack evaluator instead of a recursive-descent
parser + expression tree.

High-level flow:
  - Two stacks: `values` (numbers) and `ops` (pending operators/`(`).
  - Scan the string once, left to right:
      * a run of digits is parsed as a number and pushed onto `values`.
      * `(` is pushed onto `ops`.
      * `)` pops and applies operators (via applyTop) until the matching
        `(` is found, then discards the `(`.
      * an operator (+, -, *, /): if it's a `+`/`-` in UNARY position
        (index 0, or the previous non-whitespace char is `(` or another
        operator), a `0` is pushed onto `values` first, turning `-x` into
        `0 - x` — reusing the same binary apply() instead of a separate
        unary code path. Then, while the operator on top of `ops` has
        precedence >= the current operator's precedence, pop and apply it
        (this enforces left-associativity and lets lower/equal-precedence
        pending ops resolve before a new one is pushed); finally push the
        current operator.
  - After the scan, apply any remaining operators left on `ops`.
  - precedence('*'/'/') = 2, precedence('+'/'-') = 1, precedence('(') = 0
    — '(' having the lowest precedence means it is never popped by the
    precedence check above; it is only ever removed by its matching `)`,
    which is what lets parentheses override normal precedence.
  - applyTop pops one operator and two operands (b then a, since a was
    pushed first), computes `a operator b`, and pushes the result back
    onto `values` — `/` truncates toward zero via Java's native int
    division, matching #8's behavior.
  - No tree, no recursion for precedence: nesting from parentheses is
    handled entirely by the `ops` stack holding `(` markers, and operator
    precedence is handled by the pop-while-loop instead of grammar levels.

Time complexity:  O(n)   (single left-to-right scan of the expression,
                   n = expression length)
Space complexity: O(n)   (values/ops stacks, bounded by nesting depth /
                   number of tokens)


Why the same trick doesn't transfer between problems
------------------------------------------------------
- Union-Find/Kruskal (#4) only works for MINIMAX objectives with
  UNDIRECTED edge weights. minimumElevationGain has an ADDITIVE (sum)
  objective and DIRECTIONAL edge costs (uphill != downhill), so there is
  no single scalar weight to sort/union on, and MST paths do not
  minimize path sums in general.
- Binary search + static-connectivity DFS (#3) only works because
  minimax feasibility ("is there a path with bottleneck <= mid") is
  path-independent — it only depends on which edges are allowed, not on
  the route taken. minimumElevationGain's feasibility ("is there a path
  with total sum <= mid") is path-dependent (whether an edge is
  affordable depends on budget already spent getting there), so a
  permanent-visited-once DFS would be incorrect; it would require
  tracking (cell, remaining budget) as search state, which is far more
  expensive and loses the clean O(V) per binary-search-iteration bound.


Summary table
-------------
Method                       | Time            | Space
------------------------------|-----------------|-------
minimumElevationGain          | O(V log V)      | O(V)
minimumEffortPathDijkstra     | O(V log V)      | O(V)
minimumEffortPathDfs          | O(V log(range)) | O(V)
minimumEffortPath             | O(V log V)      | O(V)
findPath                      | O(V)            | O(V)
maximumSafenessFactor         | O(V log V)      | O(V)
canReachDestinationBFS        | O(V^2 log V)    | O(V^2)  (V = #routers)
TowerReachable.calculate      | O(n)            | O(n)    (n = expr length)
StackExpressionEvaluator.calc | O(n)            | O(n)    (n = expr length)