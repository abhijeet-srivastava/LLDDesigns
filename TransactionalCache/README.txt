## Project Structure
In memory Key-Value pair cache, where keys are of Type String, and Keys can be any Object type.
Cache supports fallowing methods-
1. get(String key) : Return an Object corresponding to given key, or else null
2. put(String key, Object value): Set a value object corresponding to a key, for existing key, override value
3. delete(String key): delete a key from cache
4. transaction: Start a new transaction, if any transaction is in progress, its state is saved and a new Transaction become active, returns integer transaction id
5. commit: Commits, existing transaction, and make it cahnges visible in preceeding transaction
6. rollback: Rollback changes done in existing transaction
7. If commit or rollback are called without any transaction, it will throw run time exception.
Cache should support Transactional behavior, where calling