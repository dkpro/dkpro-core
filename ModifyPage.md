---
layout: page
title: "Howto"
permalink: "/howto/"
sidebar: right
---

Besides adding content, you may want to alter different aspects of the page to fit to your project.

You can easily modify:

 - the navigation bar
 - the default look of the frontpage
 - the sidebar
 - the footer


Adding content pages
--------------------
In the folder `pages`, copy the file `example.md` and rename it.
Have a look at it, and modify it according to your needs. Every such file has to start with **front matter**,
i.e. two lines containing only `---`, between those go settings for the page (title, layout, etc.).
After that, just write your content in [Markdown syntax][1]

You now may want to change the navigation, to make your new page accessible.


Customizing the navgiation bar
------------------------------
Open `_data/navigation.yml` and adapt it to your needs - it should be mostly self-explanatory.
A top level entry looks like this:

	- title: Downloads
	  url: "/downloads/"
	  side: left

You can also add a dropdown attribute.


Changing frontpage look
-----------------------
The content for your frontpage goes into the `index.md` in the root directory.
If you want the frontpage to hold special elements (like the icon table for the DKPro Core page),
you need to edit `_layouts/frontpage.html`.

Otherwise you can just change the `layout` of the frontpage inside `index.md`, e.g. to `page` or `page-fullwidth`.
Enabling the sidebar is as easy as adding an attribute `sidebar: right` (should be used with `layout: page`) to your frontmatter.


Sidebar content
---------------
To change the sidebar content (widgets etc.), edit `_includes/sidebar.html`.


Changing the footer
-------------------
Changing the footer involves editing the following files: `_data/services.yml` and `_data/network.yml`.

You might also want to change the `description` field in `_config.yml`, the content of which is shown in the footer on the left side.
If you want to change the "More" link which links to an info site, open `_includes/footer.html` and
find the line `<a href="{{ site.url }}/info/">{{ site.data.language.more }}</a>`; either change the link (e.g. removing `info/`, which links to your start page) or remove it altogether. Otherwise edit `pages/info.md`.

You can remove and add social media buttons to the subfooter using the file `_data/socialmedia.yml`.


Sitewide configuration
----------------------
In the sites' root directory, edit `_config.yml` - the most important settings are under the first caption, `Site Settings`.

[1]: http://daringfireball.net/projects/markdown/syntax
