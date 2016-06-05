CircularReveal
==============

Lollipop ViewAnimationUtils.createCircularReveal for everyone 14+

<a href="http://www.youtube.com/watch?feature=player_embedded&v=tPjpF75-BWA
" target="_blank">Yotube Video <br /> <img src="http://img.youtube.com/vi/tPjpF75-BWA/0.jpg"
alt="Circular Reveal" width="320" height="240" border="10" /></a>

####[Checout demo application ](https://github.com/ozodrukh/CircularReveal/releases)


How to use:
======

Use regular `RevealFrameLayout` & `RevealLinearLayout` don't worry, only target will be clipped :)

```xml
<io.codetail.widget.RevealFrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Put more views here if you want, it's stock frame layout  -->

    <android.support.v7.widget.CardView
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/awesome_card"
        style="@style/CardView"
        app:cardBackgroundColor="@color/material_deep_teal_500"
        app:cardElevation="2dp"
        app:cardPreventCornerOverlap="false"
        app:cardUseCompatPadding="true"
        android:layout_marginLeft="8dp"
        android:layout_marginRight="8dp"
        android:layout_marginTop="8dp"
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:layout_gravity="center_horizontal"
        />

</io.codetail.widget.RevealFrameLayout>
```

```java

    View myView = findView(R.id.awesome_card);

    // get the center for the clipping circle
    int cx = (myView.getLeft() + myView.getRight()) / 2;
    int cy = (myView.getTop() + myView.getBottom()) / 2;

    // get the final radius for the clipping circle
    int dx = Math.max(cx, myView.getWidth() - cx);
    int dy = Math.max(cy, myView.getHeight() - cy);
    float finalRadius = (float) Math.hypot(dx, dy);

    // Android native animator
    Animator animator =
            ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.setDuration(1500);
    animator.start();

```

How to add dependency
=====================

This library is not released in Maven Central, but instead you can use [JitPack](https://www.jitpack.io/)

add remote maven url

```groovy
	repositories {
	    maven {
	        url "https://jitpack.io"
	    }
	}
```

then add a library dependency

```groovy
	dependencies {
	    compile ('com.github.ozodrukh:CircularReveal:2.0.1@aar') {
	        transitive = true;
	    }
	}
```


License
--------

    The MIT License (MIT)

    Copyright (c) 2016 Abdullaev Ozodrukh

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
