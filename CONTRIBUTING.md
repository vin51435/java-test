# Contributing to Manasvia Backend

Thank you for contributing to **Manasvia Backend**! To maintain clean commit histories, deterministic builds, and modular architecture, please adhere to these guidelines.

---

## Quick Reference

1. **Active Branch**: All initial setup and foundation work lives on [`setup`](file:///C:/Users/v3p51/Projects/Manasvia/backend).
2. **Branch Naming**: Feature and fix branches must follow `<type>/<module>-<short-description>`.  
   *Example*: `feat/identity-guest-login`, `fix/jwt-expiration-timezone`.
3. **Commit Messages**: Must follow **Conventional Commits 1.0** specification.  
   *Example*: `feat(identity): implement anonymous guest token generation`.
4. **Git Hooks**: Native Husky-equivalent hooks are activated via:
   ```powershell
   .\gradlew.bat installHooks
   ```

---

## Detailed Documentation

For full architectural guidelines, branching strategies, allowed scopes, and Git hooks documentation, please refer to the dedicated guide:

* **[Git Workflow and Commit Guide](docs/git-workflow-and-commit-guide.md)**
* **[Developer Handbook & Architecture Masterclass](../documentation/backend_architecture_and_system_design.md)**

---

## Daily Workflow Summary

```powershell
# 1. Switch to a new branch for your task
git checkout -b feat/<module>-<description>

# 2. Make your changes and test locally
.\gradlew.bat dev

# 3. Stage and commit following Conventional Commits
git add <changed-files>
git commit -m "<type>(<scope>): <short imperative description>"

# 4. Push to remote
git push origin feat/<module>-<description>
```
