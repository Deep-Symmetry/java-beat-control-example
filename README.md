# java-beat-control-example

[![project chat](https://img.shields.io/badge/chat-on%20zulip-brightgreen)](https://deep-symmetry.zulipchat.com/#narrow/stream/278354-carabiner)

A Java example program showing how to implement something like the Clojure-based
[open-beat-control](https://github.com/Deep-Symmetry/open-beat-control) (without
the OSC features) in Java. This is intended to be a starting point for projects
that want to implement subsets of the features of
[beat-link-trigger](https://github.com/Deep-Symmetry/beat-link-trigger), perhaps
as a headless (non-GUI) application, or offering an alternative user interface:
perhaps using GPIO pins on a tiny system like a Raspberry Pi.

Please feel free to examine, fork, and customize as you see fit!

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%202.0-blue.svg)](#licenses)

## Usage

This project is set up to use Maven. To compile it you can execute:

    mvn package

This will produce an “überjar” containing the project code along with all required dependencies,
in the file `target/jcbe-jar-with-dependencies.jar`. You can run it like this:

    java -jar target/jbce-jar-with-dependencies.jar

You can also pass command-line options after the jar file name, just like Open
Beat Control (in fact, a large part of the example code involves how to handle
such options):

### Options

    usage: java -jar jcbe-jar-with-dependencies.jar
     -a,--ableton-master         When bridging, Ableton Link tempo wins
     -B,--bridge                 Use Carabiner to bridge to Ableton Link
     -b,--beat-align             When bridging, sync to beats only, not bars
     -c,--carabiner-port <arg>   When bridging, port # of Carabiner daemon
                                 (default 17000)
     -d,--device-number <arg>    Use fixed device # (overrides -r)
     -h,--help                   Display help information and exit
     -l,--latency <arg>          How many milliseconds are we behind the CDJs
                                 (default 20)
     -r,--real-player            Try to pose as a real CDJ (device #1-4)

### Building on this Framework

There are some comments towards the end of the `Main` class pointing at how
this could grow to contain additional features that Open Beat Control offered,
but using the interfaces or protocols that you need, with links to the Clojure
implementations in OBC. Hopefully those will be easier to understand now that
you can compare this Java implementations of the rest of OBC with its Clojure
source.

### Logging Configuration

Deep Symmetry’s Java libraries use [slf4j](http://www.slf4j.org/manual.html) to allow
you to integrate it with whatever Java logging framework your project
is using. This example configures it by including slf4j-jdk14 binding on
the class path to simply redirect logging to the Java 1.4 logging facility.
If you want to do something fancier, feel free to switch to a different
binding (this is done in the [Maven project descriptor](pom.xml)),
and add whatever configuration you’d like.

## Getting Help

<a href="http://zulip.com"><img align="right" alt="Zulip logo" src="doc/assets/zulip-icon-circle.svg" width="128" height="128"></a>

Deep Symmetry&rsquo;s projects are generously sponsored with hosting
by [Zulip](https://zulip.com), an open-source modern team
chat app designed to keep both live and asynchronous conversations
organized. Thanks to them, you can [chat](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger)
with our community</a>, ask questions, get inspiration, and share your
own ideas. You can also open an
[issue](https://github.com/Deep-Symmetry/beat-carabiner/issues) on the
project if you have found something that seems to need fixing.

## Research

This project is being developed with the help of
[dysentery](https://github.com/Deep-Symmetry/dysentery). Check that
out for details of the packets and protocol, and for ways you can help
figure out more. It makes heavy use of
[beat-carabiner-java](https://github.com/Deep-Symmetry/beat-carabiner-java),
which is in turn built on [beat-link](https://github.com/Deep-Symmetry/beat-link)
and [Carabiner](https://github.com/Deep-Symmetry/carabiner).

## Downstream Projects

I will gather links to projects I hear about that are building on this example
here. Please
[let me know](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger)
if you are experimenting with this so I can include you!

* [java-bc](https://github.com/kokospalme/java-BC) uses a Pi single-board computer 
  with an OLED display to provide a compact system for managing synchronization
  between Pro DJ Link and Ableton Link networks.


### Funding

Java Beat Control Example is, and will remain, completely free and open-source. If it
has helped you, taught you something, or inspired you, please let us
know and share some of your discoveries and how you are using it! If
you'd like to financially support its ongoing development, you are
welcome (but by no means obligated) to donate to offset the hundreds
of hours of research, development, and writing that have already been
invested. Or perhaps to facilitate future efforts, tools, toys, and
time to explore.

<a href="https://liberapay.com/deep-symmetry/donate"><img align="center" alt="Donate using Liberapay"
src="https://liberapay.com/assets/widgets/donate.svg"></a> using Liberapay, or
<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=J26G6ULJKV8RL"><img align="center"
alt="Donate" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif"></a> using PayPal

> If enough people jump on board, we may even be able to get more
> gear to experiment with, although that's an unlikely stretch goal.
> :grinning:

### Contributing in Other Ways

If you have ideas, discoveries, or even code you’d like to share,
that’s fantastic! Please take a look at the
[guidelines](https://github.com/Deep-Symmetry/beat-link/blob/main/CONTRIBUTING.md) and get in touch!

## Licenses

<a href="http://deepsymmetry.org"><img align="right" alt="Deep Symmetry" src="doc/assets/DS-logo-github.png" width="250" height="150"></a>

Copyright © 2023 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the [Eclipse Public License
2.0](https://opensource.org/licenses/EPL-2.0). By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software. A copy of the license can be found in
[LICENSE.md](https://github.com/Deep-Symmetry/beat-carabiner/blob/master/LICENSE.md)
within this project.

The included copies of Carabiner are distributed under the [GNU
General Public License, version
2](https://opensource.org/licenses/GPL-2.0). A copy of the license can be found in
[gpl-2.0.md](https://github.com/Deep-Symmetry/beat-carabiner/blob/master/gpl-2.0.md)
within this project.
