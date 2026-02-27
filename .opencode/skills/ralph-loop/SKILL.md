---
name: ralph-loop
description: Detect requests for iterative AI task loops and invoke the Ralph command
version: 2.0.0
triggers:
  - "ralph this"
  - "ralph:"
  - "ralph it"
  - "keep trying until"
  - "loop until"
  - "iterate until"
  - "run until passes"
  - "fix until green"
  - "keep fixing until"
  - "keep going until"
  - "iterate on"
---

# Ralph Loop Skill

You detect when users want iterative task execution and route to the `/ralph` command.

## Trigger Patterns

| Pattern | Example | Action |
|---------|---------|--------|
| `ralph this: X` | "ralph this: fix all lint errors" | Extract task, infer completion |
| `ralph: X` | "ralph: migrate to TypeScript" | Extract task, infer completion |
| `ralph it` | "ralph it" (after task description) | Use conversation context |
| `keep trying until X` | "keep trying until tests pass" | Task = current context, completion = X |
| `loop until X` | "loop until coverage >80%" | Task = improve coverage, completion = X |
| `iterate until X` | "iterate until no errors" | Task = fix errors, completion = X |
| `run until passes` | "run until passes" | Infer test command |
| `fix until green` | "fix until green" | Task = fix tests, completion = tests pass |
| `keep fixing until X` | "keep fixing until lint is clean" | Task = fix lint, completion = X |

## Extraction Logic

### Task Extraction

**From explicit task**:
- "ralph this: fix all TypeScript errors" → Task: "fix all TypeScript errors"
- "ralph: migrate src/ to ESM" → Task: "migrate src/ to ESM"

**From context**:
- "ralph it" after discussing a refactor → Use previous conversation as task context

### Completion Inference

When user doesn't specify explicit verification:

| Task Pattern | Inferred Completion |
|--------------|---------------------|
| "fix tests" | "npm test passes" |
| "fix lint" / "fix linting" | "npm run lint passes" |
| "fix types" / "fix TypeScript" | "npx tsc --noEmit passes" |
| "fix build" | "npm run build succeeds" |
| "add tests" | "test coverage increases" |
| "migrate to ESM" | "node runs without errors" |
| "refactor X" | "npm test passes" (preserve behavior) |

### Examples

**User**: "ralph this: migrate all files in lib/ to ESM"
**Extraction**:
- Task: "migrate all files in lib/ to ESM"
- Completion (inferred): "node --experimental-vm-modules lib/index.js runs without errors"

**Action**: Invoke `/ralph "migrate all files in lib/ to ESM" --completion "node --experimental-vm-modules lib/index.js succeeds"`

---

**User**: "keep fixing until the tests are green"
**Extraction**:
- Task: "fix failing tests" (from context or implied)
- Completion: "npm test passes with 0 failures"

**Action**: Invoke `/ralph "fix failing tests" --completion "npm test passes"`

---

## Implementation

When triggered, invoke the Ralph command with extracted task and completion criteria:

```
/ralph "<task>" --completion "<verification command>"
```

If no `/ralph` command is available, you can implement a basic Ralph loop yourself:

1. Create a prompt file (PROMPT.md) with the task
2. Run your agent with the prompt
3. Check completion condition
4. If not complete, loop back to step 1
5. Stop after max iterations or when complete
