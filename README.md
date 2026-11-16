# Sam Herbst | CS 499 ePortfolio

This is the source for my ePortfolio for **CS 499: Computer Science Capstone** at Southern New Hampshire University (SNHU).

**https://samherbst0.github.io**

The portfolio takes three projects from earlier courses, reviews them, enhances them, and documents the results.

## Artifacts

| Category | Artifact | Original course | Language and tools |
|---|---|---|---|
| Software Design and Engineering | Weight Tracker Android app | CS-360: Mobile Architecture and Programming | Java, SQLite, Android |
| Algorithms and Data Structures | ABCU Course Planner | CS-300: Data Structures and Algorithms | C++ |
| Databases | Grazioso Salvare Animal Shelter Dashboard | CS-340: Client/Server Development | Python, MongoDB, Dash |

## Progress

| Item | Status |
|---|---|
| Code review (video and written summary) | Published on the [Code Review page](https://samherbst0.github.io/code-review/) |
| Enhancement One: Software Design and Engineering | Complete, to be published |
| Enhancement Two: Algorithms and Data Structures | Complete, to be published |
| Enhancement Three: Databases | Planned |
| Professional self-assessment | Planned |

## Repository layout

| Path | Contents |
|---|---|
| `index.md` | Home page |
| `code-review.md` | Code review page, with the video and a summary for each artifact |
| `assets/video/` | The code review video (720p copy compressed for the web) |
| `reference/` | The original, unenhanced version of each artifact, kept for before-and-after comparison |
| `_config.yml` | Jekyll site settings (uses the `minima` theme) |

## Notes

- The database password in the original CS-340 files is masked as `********`. Credentials will not be committed to this repository.
- Some starter files came from the courses: the Android project template for CS-360 and the dashboard structure for CS-340. All other code is my own work.
- Nothing from my employment is included in this portfolio.

## Running the site locally

GitHub Pages builds the site automatically when changes are pushed to `main`. To preview it locally, install Ruby and Jekyll, then run:

```bash
gem install jekyll minima
jekyll serve
```

Then open http://localhost:4000.
