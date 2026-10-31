# Anchor AI system

## What it does

- Enriches short captures into task or note records, with dates, times, priority, tags, and duration when supported by the text.
- Answers questions from a bounded selection of the user's active tasks and notes, returning only matching database records as references.
- Builds a morning focus list, an evening review, and small steps for repeatedly snoozed tasks.
- Uses local heuristics when Firebase AI is unavailable or a response is invalid.
- Checks a new capture against recent active items and offers to open the existing item or save both.
- Builds a reviewable Inbox triage plan; it never moves items until the user selects and accepts them.
- Lets users request a micro-step plan from any task detail, without waiting for repeated snoozes.

The remote model is `gemini-3.8-flash`, listed as a supported Firebase AI Logic model in the [Firebase model guide](https://firebase.google.com/docs/ai-logic/models).

## Reliability and data boundaries

- Captures longer than 6,000 characters skip the remote parse and use the local parser. Context queries are capped at 1,000 characters; model responses are capped before JSON parsing, and capture splitting is limited to 12 records.
- Duplicate checks are local and compare a new capture with up to 200 recent active parent items; they do not block the user's choice to save it anyway.
- Task and note data is passed as JSON and described to the model as untrusted evidence. Returned IDs are checked against actual active records, and the morning brief's duration total is recomputed from those records.
- Task dates, times, priorities, tags, and estimates are normalized before persistence. Notes do not create due dates or reminders.
- The local parser recognizes today/tomorrow, next week, upcoming weekdays, relative day/week offsets, weekends, common clock times, noon, midnight, and tonight.
- User edits, moving an item to Today, completing it, and snoozing it protect the item from pending AI enrichment. The database update checks that marker atomically, so a slow response cannot overwrite those actions or create leftover split tasks.
- AI failures log the exception class without logging captured task or note text.

When Firebase AI is configured, capture text and the selected task or note context are sent to Firebase AI Logic to produce these features. The app falls back to local heuristics when that service is unavailable.
