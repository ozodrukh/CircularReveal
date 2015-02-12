CircularReveal
==============

Lollipop ViewAnimationUtils.createCircularReveal for everyone 2.3+

<img src="http://7sbnrp.com1.z0.glb.clouddn.com/lollipop2-CircularReveal.gif" />

<a href="http://www.youtube.com/watch?feature=player_embedded&v=_vVpwzYb4Dg
" target="_blank">Yotube Video <br /> <img src="http://img.youtube.com/vi/_vVpwzYb4Dg/0.jpg" 
alt="Ripple DEMO" width="320" height="240" border="10" /></a>

Sample
======
<a href="https://github.com/ozodrukh/CircularReveal/releases"> Sample & .aar file </a>

Note
====

depends from Jake Wharton's NineOldsAndroid, or use my modifed version (included auto cancel)

Using
======

Use regular `RevealFrameLayout` & `RevealLinearLayout` don't worry, only target will be clipped :)

```xml
<io.codetail.widget.RevealFrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Put more views here if you want, it's stock frame layout from Lollipop :)   -->

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
    int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

    SupportAnimator animator =
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
	    compile 'com.github.ozodrukh:CircularReveal:(latest-release)@aar'
	}
```

License
--------

    The MIT License (MIT)

    Copyright (c) 2014 Abdullaev Ozodrukh
    
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
I
