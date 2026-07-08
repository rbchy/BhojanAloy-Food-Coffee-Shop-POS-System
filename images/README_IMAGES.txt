Put item photos here (e.g. espresso.jpg, cappuccino.jpg, plain_bagel.png).

There are no real food photos bundled with this project — no internet
image source was used, so nothing here could be a copyrighted photo you
didn't choose yourself. Until you add your own photos, every item in the
POS and Menu Management screens simply shows a large category emoji
(☕ 🥯 🍩 🥪 🥤 etc.) instead, so the UI still looks visually distinct.

To add a real photo for an item:
1. Save the photo file into this "images" folder, e.g.:
     images/espresso.jpg
2. In the app, go to Menu Items -> Edit (or Add) the item, and set
   "Image Path" to:
     images/espresso.jpg
3. Save. The photo now appears on that item's card in the POS screen
   automatically (resized to fit), replacing the emoji fallback.

Recommended: square photos, at least 300x300px, JPG or PNG.

login_banner.png:
This is a simple illustrated coffee-shop storefront (generated with code,
not a real photo — same "no unlicensed photo" reasoning as above) shown at
the top of the Login screen. Replace it with your own banner any time by
overwriting images/login_banner.png with your own image (recommended size
around 760x320px, PNG or JPG) — LoginFrame.java loads whatever is there.
