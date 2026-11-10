#!/usr/bin/env bash
# The web demo reuses the desktop app's data layer as copies (webapp/README.md).
# Fail if a copy has drifted from the original, so a fix in one place cannot be lost in the other.
set -euo pipefail
cd "$(dirname "$0")/.."
status=0
for f in config/DBConnection dao/MenuItemDAO dao/OrderDAO dao/UserDAO \
         model/MenuItem model/Order model/OrderItem model/Payment model/User \
         service/POSService util/PasswordUtil; do
  if ! diff -q "src/com/bhojanaloy/$f.java" "webapp/src/main/java/com/bhojanaloy/$f.java" > /dev/null; then
    echo "DRIFT: $f.java differs between src/ and webapp/"
    diff -u "src/com/bhojanaloy/$f.java" "webapp/src/main/java/com/bhojanaloy/$f.java" | head -40 || true
    status=1
  fi
done
[ $status -eq 0 ] && echo "Shared code in sync (11 files)"
exit $status
