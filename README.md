# resu-me

`resu-me` is a command-line tool that generates a PDF resume from a `.toml` configuration file using LaTeX. given a configuration file with various sections (like personal details, summary, etc.), resu-me produces a corresponding .tex file and (optionally) a PDF using pdflatex.

this tool leverages clojure's functional programming features to parse the configuration file and apply the data to a predefined LaTeX resume template.

## requirements and installation

1. install [clojure](https://clojure.org/guides/getting_started), which is required to run the project, and [leiningen](https://leiningen.org/), which is used to manage dependencies and build the project.

2. installing LaTeX is optional but **highly recommended**. if you wish to generate pdfs, a LaTeX distribution (such as pdflatex) is required. Otherwise, you can skip pdf generation and only generate .tex files, and use an online LaTeX editor such as *Overleaf*.

3. clone this repository:

```bash
git clone https://gitlab.com/bigbookofbug/resu-me
cd resu-me
```

4. install dependencies using leiningen:

```bash
lein deps
```

## usage

you can run `resu-me` directly using leiningen, or you can build a standalone jar file.
running with leiningen requires being in the project directory, and is done like so:

```bash
lein run -- [args]
```

### Building a JAR

to build the project into a standalone JAR file:

```bash
lein uberjar
```

then, you can run the JAR file as follows:

```bash
java -jar path/to/resu-me-0.1.0-standalone.jar [args]
```

## options

* `--help`, `-h`: displays help and usage information.
* `--config=CONFIG`, `-c CONFIG`: specify the path to the .toml configuration file. If not provided, the program searches for resume.toml in the current directory.
* `--no-pdf`: skip pdf generation. The program will only generate the .tex file without running pdflatex.

## examples

**IMAGES TO COME STAY TUNED**

### example .toml config:

```toml
[Template]
style = "Meta"
template = "bugstyle"

[Personal]
style = "banner"
title = "Bug Bugson"
list = [
    "555-555-5555",
    "ema@ail.com",
    "mywebsite.site"
]

[Summary]
style = "summary"
list = [
	"This is a summary of myself. This summary takes the form of a short paragraph that will go over relevant interests and skillsets. This can be used as a sort of \"elevator pitch\" for oneself, and should provide an overview of what one can do."
]
```

### generated LaTeX output

```latex
\documentclass[a4paper, 12pt]{article}
\usepackage{setspace}
\usepackage{multicol}
\usepackage{times}
\usepackage{soul}
\usepackage[left=2.5cm, top=2.5cm, right=2.5cm, bottom=2.5cm, bindingoffset=0.5cm]{geometry}
\usepackage[utf8]{inputenc}
\renewcommand{\baselinestretch}{1}
\pagenumbering{gobble}

\begin{document}

\begin{flushright}
\setstretch{0.5}{\fontsize{12pt}{12pt}\selectfont
\item 555-555-5555
\item ema@ail.com
\item mywebsite.site}
\end{flushright}

\vspace{-40pt}

\begin{flushleft}
\setstretch{0.5}{\fontsize{16pt}{16pt}\selectfont
\textbf{Bug Bugson}}
\noindent\rule{\textwidth}{0.4pt}
\end{flushleft}

\begin{flushleft}
\setstretch{0.5}{\fontsize{12pt}{12pt}\selectfont
This is a summary of myself. This summary takes the form of a short paragraph that will go over relevant interests and skillsets. This can be used as a sort of "elevator pitch" for oneself, and should provide an overview of what one can do.}
\newline
\end{flushleft}

\end{document}
```

### Bugs

(me . i'm bigbook of bug)

## Contributing

feedback and contributions are welcome! `resu-me` is currently in the early testing stages, and i'm eager to improve it based on community input. bug reports, advice on improing user-friendliness, requests for new templates, and any and all other forms of feedback are welcome ! feel free to create issues or submit pull requests :)

### goals

- [ ] better name (please guys help me here)
- [x] three templates
- [ ] example images
- [ ] three seasons and a movie

## License

Copyright © 2024 big bug

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
