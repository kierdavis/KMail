KMail is a plugin that allows text messages to be sent to other player, regardless of whether they are online. Messages can also be sent between servers. It is inspired by real-life e-mail and by turt2live's [xMail][xmail] plugin.

[xmail]: http://dev.bukkit.org/server-mods/xmail

# Usage

To send a message to a player named "bob":

    /kmail send bob Hello there!

To read an unread message:

    /kmail read

To list all messages in your mailbox:

    /kmail list

To list unread messages (i.e. messages with the tag "unread"):

    /kmail list t:unread

# Commands

* `/kmail help [topic]` - View help
* `/kmail send <address> [message]` - Send a message
* `/kmail list [criteria] [page]` - List messages (optionally refined by search criteria)
* `/kmail select <id>` - Select a message to be acted upon
* `/kmail read [id]` - Reads either the selected message or one specified by its ID and marks it as read
* `/kmail read next` - Reads the first unread message and marks it as read
* `/kmail tag [id] <tags...>` - Add tags to a message
* `/kmail untag [id] <tags...>` - Remove tags from a message

For full command help, refer to the in-game help system (via the `/kmail help` command).

# Permissions

* `kmail.help` - Gives access to view the builtin help (`/kmail help`)
* `kmail.send` - Gives access to send mail (`/kmail send`)
* `kmail.send.remote` - Gives access to send mail to remote servers
* `kmail.list` - Gives access to list and search mail
* `kmail.select` - Gives access to select messages
* `kmail.read` - Gives access to read mail
* `kmail.tag` - Gives access to tag and untag messages

# Development Builds

Development builds can be found on the [build server][build-server].

**Use these at your own risk. They are not guaranteed to have been fully tested.**

[build-server]: http://bukkit.kierdavis.com/KMail/
