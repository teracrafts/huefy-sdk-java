# Huefy Java SDK Lab

Verifies the core email contract through the real `HuefyEmailClient` against a local stub server.

## Run

```bash
mvn compile exec:java -Plab -q
```

from `sdks/java/`.

## Scenarios

1. Initialization
2. Single-send contract shaping
3. Bulk-send contract shaping
4. Invalid single rejection
5. Invalid bulk rejection
6. Health request path behavior
7. Cleanup

## Notes

- The lab exercises real email-client methods, not generic infrastructure helpers.
- It verifies request keys, normalization, parsed responses, and validation-before-transport behavior.
