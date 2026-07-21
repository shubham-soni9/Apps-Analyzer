# AppsAnalyzer — Website

The marketing site for **AppsAnalyzer**, built with [Astro](https://astro.build/).

It contains:

- **`/`** — Landing page
- **`/privacy`** — Privacy Policy
- **`/terms`** — Terms & Conditions

## Commands

All commands are run from this `web/` directory:

| Command           | Action                                        |
| :---------------- | :-------------------------------------------- |
| `npm install`     | Install dependencies                          |
| `npm run dev`     | Start local dev server at `localhost:4321`    |
| `npm run build`   | Build the production site to `./dist/`        |
| `npm run preview` | Preview the production build locally          |

## Deployment

`npm run build` outputs a fully static site to `web/dist/` that can be hosted on any
static host (Netlify, Vercel, GitHub Pages, Cloudflare Pages, etc.).

Before deploying, set the production domain in [`astro.config.mjs`](astro.config.mjs)
(the `site` field) and confirm the download link in `src/pages/index.astro` points at
your published APK.
