# Contributing Guidelines

Thank you for considering contributing to the ComposeHooks project! This document provides guidelines for participating in project development.

## Code of Conduct

Please respect all project participants and maintain a professional and friendly communication environment.

## How to Contribute

### Reporting Issues

If you've found a bug or have a feature suggestion, please submit it through GitHub Issues and provide as much information as possible:

- Clear description of the issue
- Steps to reproduce
- Expected behavior vs actual behavior
- Relevant logs or screenshots
- Your environment information (OS, Kotlin version, Compose version, etc.)

### Submitting Code

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m '✨: add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Create a Pull Request

### Code Style Guidelines

1. **File Header Declaration**:
```kotlin
/*
  Description: Brief description of the file
  Author: Your Name
  Date: ${DATE}-${TIME}
  Email: your.email@example.com
  Version: v1.0
*/
```

2. **Hook Implementation Guidelines**:
- Return value should be named `XxxxHolder`
- Ensure stability of the `Holder`
- Do not return state values directly, wrap them in `State`
- Put `State` first in the holder, followed by functions
- Prefer using existing hooks over native Compose functions:
  - Use `useState` instead of `derivedStateOf`
  - Use `useCreate` or `useRef` instead of `remember`
  - Use `useEffect` for side effects instead of `LaunchedEffect`
- Use destructuring declaration syntax for holder function calls
- Declare type aliases for function members

3. **State Management**:
- Use `State` wrapper for all state values
- Follow Compose lifecycle guidelines
- Avoid unnecessary recompositions
- Ensure proper resource cleanup
- Handle edge cases and errors appropriately

### Git Commit Message Guidelines

To maintain consistency and readability in commit history, please follow these commit message guidelines:

1. **Use Gitmoji**: Each commit message should start with a relevant Gitmoji indicating the type of commit.

2. **Common Gitmoji**:
   - ✨ `:sparkles:` - Add new feature/hook
   - 🐛 `:bug:` - Fix bug
   - 📝 `:memo:` - Update documentation/comments
   - ⚡️ `:zap:` - Refactor/optimize code
   - 🧑‍💻 `:technologist:` - Update example code
   - 🩹 `:adhesive_bandage:` - Fix/optimize/clean up code
   - ⬆️ `:arrow_up:` - Update dependencies
   - 🔖 `:bookmark:` - Version tags
   - 👷 `:construction_worker:` - CI related changes
   - 💡 `:bulb:` - Code/comment modifications
   - 💥 `:boom:` - Breaking changes/migration
   - 🤖 `:robot:` - Code cleanup/migration
   - 🎨 `:art:` - Code formatting/structure optimization
   - 🔥 `:fire:` - Remove code
   - 🧪 `:test_tube:` - Add tests
   - 💩 `:poop:` - Remove/refactor unreasonable code

3. **Message Format**:
```
[Gitmoji] [Module]: Short description (max 50 chars)

Detailed description (optional, max 72 chars per line)
```

4. **Examples**:
```
✨ [Network]: Add network state hook

Adds a hook for getting network state, including connection type and availability.

🐛 [Form]: Fix validation error in email field

Fixes an issue where email validation was not properly handling special characters.

📝 [Docs]: Update README with new hook examples

Adds comprehensive examples for useRequest and useForm hooks.
```

5. **Best Practices**:
   - Always use the most specific gitmoji that matches your changes
   - Keep the first line under 50 characters
   - Use the imperative mood in the first line
   - Provide a detailed description when the change is complex
   - Reference issues and pull requests liberally after the first line
   - Consider starting the message with a verb
   - Don't end the first line with a period
   - Use the body to explain what and why vs. how

### Pull Request Guidelines

- Ensure PR title clearly describes the changes
- Provide detailed description of your changes in the PR, including the problem solved or feature added
- If your PR addresses an issue, link to it using keywords (e.g., `Fixes #123`)
- Ensure all tests pass
- Update relevant documentation if necessary

## Development Guidelines

### Hook Development Standards

1. **Naming Conventions**:
   - Hook function names should start with `use`, e.g., `useNetwork`
   - Names should clearly express functionality, avoid abbreviations

2. **Documentation Requirements**:
   - Each hook must have complete KDoc documentation
   - Documentation should include function description, parameter descriptions, return value description, and usage examples

3. **Testing Requirements**:
   - Write unit tests for each hook
   - Tests should cover normal usage scenarios and edge cases
   - Include test cases for error conditions and edge cases

### Example Code

Provide example code for new hooks, demonstrating basic usage and common scenarios. Examples should be concise and easy to understand.

## License

By contributing code, you agree that your contribution will be released under the project's license.

## Contact

For any questions, please contact through GitHub Issues or reach out to the project maintainer: junerver@gmail.com
