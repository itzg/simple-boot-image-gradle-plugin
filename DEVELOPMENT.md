## Testing

In an adjacent project, add the following to `settings.gradle`:
```
pluginManagement {
    includeBuild '../simple-boot-image-gradle-plugin'
}
```

## Publishing

Using GitHub Actions make sure that the following build secrets are declared:
- `PLUGIN_PORTAL_KEY`
- `PLUGIN_PORTAL_SECRET`