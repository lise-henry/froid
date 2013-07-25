FROID
=====

Fast Race Of Insane Drivers – A small weird driving simulation RPG game

## Installation

You don't have to install FROID to run it; if you downloaded the .jar
file, it includes all necessary libraries (that is, Clojure) and you
just have to run:

`$ java -jar froid.jar`

If you want to run it from the sources, you need to get Leiningen and
run:

`$ lein run`

(in the appropriate directory).

## Usage

Just start by creating a team, which you do by clicking on
"New". You will then be prompted a team name, and three driver names.

The next step is to raise your drivers stats (clicking "Edit drivers"
by spending XP (experience points). The different stats are:

* speed-straight: the speed of the driver in straight lines.

* speed-soft: the speed of given driver in soft turns.

* speed-hard: the speed of the driver in hard turns.

* overtake: the capacity of the driver to overtake the previous one.

* block: the capacity of the driver to block someone trying to
  overtake her.

* skill-qual: how a driver fares in qualifications.

* skill-crash: the ability of a driver to avoid crashing.

Then you just have to click the "run" button and watch how your
drivers behave. Each driver gain XP at the end of a race if they
manage not to crash; you can raise use those XP whenever you want (by
clicking on a driver name), but it will only be taken in consideration
for next race.

## Author

Élisabeth Henry <liz.henry at ouvaton dot org>

## License

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
