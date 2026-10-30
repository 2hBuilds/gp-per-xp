# 2h XP and GP

> The full description of 2h XP and GP. The short version, with pictures, is the plugin's page on the Plugin Hub (the repository's README).

Right-click a skill to see the cheapest, fastest and most profitable ways to train it: gp/xp, xp/h, gp/h and cost to your goal. 24 skills, 2,659 methods, each taken from the OSRS Wiki and keeping its wiki link.

Skills: Agility, Attack, Construction, Cooking, Crafting, Defence, Farming, Firemaking, Fishing, Fletching, Herblore, Hitpoints, Hunter, Magic, Mining, Prayer, Ranged, Runecraft, Sailing, Slayer, Smithing, Strength, Thieving and Woodcutting.

## Opening it

Right-click a skill in the Skills tab and choose "GP/XP". The entry shows only for skills that have methods. It opens the sidebar on that skill and does nothing else in the game. Or open the sidebar and pick a skill from the drop-down; the sidebar opens on the skill and view you used last.

## The sidebar, top to bottom

- The title row, with the 2hBuilds links and the settings icon.
- The skill drop-down.
- The view switch: **List | Top picks**, and **Healing** too when Hitpoints is picked.
- **My level | All levels** (List view only). Logged out, a note says "Log in to see your levels".
- **Target** and **My speed**.
- The sort boxes **gp/xp | xp/h | gp/h**, and the Search box.
- The list.

## The method list

One row per method, two lines. Line 1 is the name and its level ("Lv 30"). Line 2 is three columns: gp/xp, xp/h and gp/h.

- **Spending and earning.** In the gp columns, gp spent is red with a minus ("-3.2") and gp earned is green with a plus ("+12.4"). A method that costs nothing shows "free". xp/h is never coloured.
- **My level and All levels.** Two buttons under the view switch. "My level" (the default) lists only methods you can do. "All levels" adds the rest, greyed and after the others.
- A method above your level, under "All levels", is greyed. Its level column shows the first requirement you have not met ("Magic 90", or "Quest"). Skill and quest requirements count; diary and text requirements never hide a row. Logged out, nothing is greyed.
- A method with a missing price shows "?" and sits after the priced ones.
- While prices have not loaded, the list says "Waiting for prices..." instead of rows of "?".
- **Rates as the wiki states them.** A range reads "10-15k" (gp "-100-150k"), an "about" figure "~12k", "at least" "10k+", "up to" "<20k". "Per run" methods read "per run". A method with no stated rate shows "-" for the hourly columns. The unit (gp/xp, xp/h, gp/h) follows a figure only when it still fits the column.
- **Cost not stated.** Where the wiki does not say what a method costs, gp/xp and gp/h show "-", never "free". These rows sort after rows with a cost in the gp sorts.
- **xp/h only.** Where the wiki gives only xp per hour, the list shows it, and the card says "not stated" for xp per action and actions per hour.
- **Level bands.** Where the wiki gives different rates at different levels, the row shows the band nearest at or below your level, with its name followed by "(rate at 70)". Below every band it shows the lowest; logged out, the highest. "Show other level bands" in the settings lists every band.

## Sorting and searching

Three boxes: gp/xp, xp/h, gp/h. Click one to sort best first, with a down arrow. Click the lit one again to reverse (up arrow). The choice is remembered. Only methods you can do are reversed; "?" rows and greyed rows stay after them.

The Search box narrows the list to names containing your text, in any case. It shows an "x" while it holds text; click it, or press Escape, to clear. It is not remembered.

## Target and My speed

- **Target.** A number box, 0 to 99. 0 means your next level. The note beside it says what it does: "to level 91", "level 90 reached", or "0 = next level" when you are logged out.
- **My speed.** A number box, 10 to 100, default 100, with "% of wiki" beside it. Every xp/h, gp/h and time, on the list and the card, becomes that percent of the wiki figure, both ends of a range included. gp per xp does not change. A method with no stated rate is unchanged. Other numbers are ignored. Hover it for the same advice: lower it if you play slower than the wiki's rates.

## Top picks

Top picks is the second button of the view switch. It shows four headed sections, each with at most three rows drawn like list rows:

- **Fastest xp.** Highest xp/h (the low end of a range).
- **Best gp/h.** Most profit, or least cost, per hour.
- **Cheapest per xp.** Lowest gp/xp.
- **Wiki suggests.** Methods the skill's wiki training page recommends.

Only methods you can do now are shown, using the level band that fits you and your speed. The level buttons, the sort boxes and the Search box step aside in this view. "Include profit methods" off drops profit rows. A section with no rows is hidden. With nothing to show, the view says "No top picks for you yet.". Left-click a row to switch to the List with that method scrolled into view and highlighted for about two seconds. Logged out, everything counts as doable.

## The hover card

Hover a row to see a card beside the sidebar. From the top:

- The method name, with "<Skill>, level <n>".
- Facts: XP per action, Actions per hour, XP per hour, GP per XP, GP per hour. Actions per hour reads "1,200 (typical)", "1,000-1,500 (wiki range)", "about 1,200", "up to 2,000", "at least 1,000", "per run" or "not stated". Below 100 percent speed it reads for example "960 (80% of typical)". A fact too long for one line puts its value on the next line.
- "Needs" (skill, quest, diary or item requirements) and "You get" (what the method yields), when the wiki gives them.
- "Uses (per action)" and "Makes": each item with its picture, quantity and price; "?" when unpriced. Products are priced after the 2% Grand Exchange tax when that setting is on.
- When you are logged in and the target is above your level, "To level N": xp, actions, cost (negative and green when it makes gold) and time. Then "To buy" (each ingredient's total quantity and price for the whole trip) and "You get" (each product's total quantity and what it sells for).
- The method's note, in small grey text, up to four lines.
- A grey footer: "Wiki data of <date>" and "Prices: guide" or "Prices: live trades".

Right-click a row for "Open wiki page", which opens that method's wiki page in your browser.

## Healing (Hitpoints)

With Hitpoints picked, the view switch has a third entry, "Healing". It lists 286 foods and potions, one row each: name, gp per hp, hp per item and price. The sort boxes read gp/hp, hp and gp: cheapest gp per hp, most hp, cheapest price; click the lit one to reverse. Unpriced items show "?" and come last. The Search box works here too. Target and My speed step aside.

The card shows heals per portion (for example "16 (2 + 15% of max HP)"), portions, heals per item, price and gp per hp, the item picture, the wiki's note and the footer. Items that heal a percent of max HP use your Hitpoints level, or 99 when logged out. Right-click a row for "Open wiki page".

## Settings

Click the settings icon in the sidebar.

| Setting | What it does |
|---|---|
| Include profit methods | Lists methods that make gold. On by default. |
| Show other level bands | Lists every level band of a method. Off by default. |
| Take off the GE tax | Takes the 2% Grand Exchange tax off what products sell for. On by default. |
| Sort by | The three sort choices, the same as the sort boxes. |
| Prices | "Guide price" (default) or "Live trade prices". |
| Reset to default | Puts every setting back to its default. |
| OK | Closes the menu. |

The menu opens with the plugin's name and version and the 2hBuilds links. Every change saves as you make it. The view, My level or All levels, target, My speed and skill are set in the sidebar and remembered. The settings are not on RuneLite's settings page.

## Where the numbers come from

- **Methods.** Taken from the OSRS Wiki (https://oldschool.runescape.wiki), whose text is CC BY-NC-SA. Rates, levels and requirements are the wiki's figures, kept as the wiki states them; each row keeps its wiki page address. Nothing is estimated where the wiki gives no figure. The card's footer names the date of the data.
- **Guide price (default).** RuneLite's Grand Exchange guide price, which the client keeps up to date. The plugin makes no network request.
- **Live trade prices.** The plugin reads the wiki's public price list: one request, at most once every five minutes, only while the sidebar is showing, and at once when you switch to it. Ingredients are priced at the latest buy price and products at the latest sell price, so the list, the card and the shopping list use real margins. An item with no trade in the last 24 hours, or a price far from the guide price, uses the guide price.

## Privacy

Nothing leaves your machine unless you pick "Live trade prices". Then the only request is for the wiki's public price list; it carries nothing about you or your account, though the wiki can see your IP address. The plugin reads your skill levels and xp to grey rows and work out targets, and does not store or send them. It does not act on the game.

## Good to know

- Training does not make the sidebar flicker: it refreshes at most once every three-quarters of a second after an xp drop, and not while it is closed.
- Rates are the wiki's typical figures. Your speed will differ; use My speed.
- The only change to the game's menus is the one "GP/XP" entry on the Skills tab.

## Licence

BSD 2-Clause. See `LICENSE`. Method data: OSRS Wiki, CC BY-NC-SA.
