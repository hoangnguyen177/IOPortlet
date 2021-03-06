/*
 * BrainBrowser: Web-based Neurological Visualization Tools
 * (https://brainbrowser.cbrain.mcgill.ca)
 *
 * Copyright (C) 2011 McGill University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * BrainBrowser v1.5.2
 *
 * Author: Tarek Sherif  <tsherif@gmail.com> (http://tareksherif.ca/)
 * Author: Nicolas Kassis
 */
! function () {
    "use strict";
    var a = window.BrainBrowser = window.BrainBrowser || {};
    a.createColorMap = function (a) {
        function b(a, b, d, e) {
            var f, g, h, i = document.createElement("canvas"),
                j = new Array(256);
            for ($(i).attr("width", 256), $(i).attr("height", d), f = 0; 256 > f; f++) e ? j[255 - f] = f : j[f] = f;
            for (a = c.mapColors(j, {
                scale255: !0
            }), h = i.getContext("2d"), g = 0; 256 > g; g++) h.fillStyle = "rgb(" + Math.floor(a[4 * g]) + ", " + Math.floor(a[4 * g + 1]) + ", " + Math.floor(a[4 * g + 2]) + ")", h.fillRect(g, 0, 1, b);
            return i
        }
        var c = {
            createCanvas: function (a) {
                var d;
                return a || (a = c.colors), d = b(a, 20, 20, !1)
            },
            createCanvasWithScale: function (a, d, e) {
                var f, g, h = c.colors;
                return f = b(h, 20, 40, e), g = f.getContext("2d"), g.fillStyle = "#FFA000", g.fillRect(.5, 20, 1, 10), g.fillText(a.toPrecision(3), .5, 40), g.fillRect(f.width / 4, 20, 1, 10), g.fillText(((a + d) / 4).toPrecision(3), f.width / 4, 40), g.fillRect(f.width / 2, 20, 1, 10), g.fillText(((a + d) / 2).toPrecision(3), f.width / 2, 40), g.fillRect(3 * (f.width / 4), 20, 1, 10), g.fillText((3 * ((a + d) / 4)).toPrecision(3), 3 * (f.width / 4), 40), g.fillRect(f.width - .5, 20, 1, 10), g.fillText(d.toPrecision(3), f.width - 20, 40), f
            },
            mapColors: function (a, b) {
                b = b || {};
                var d, e, f, g, h = void 0 === b.min ? 0 : b.min,
                    i = void 0 === b.max ? 255 : b.max,
                    j = void 0 === b.scale255 ? !1 : b.scale255,
                    k = void 0 === b.brightness ? 0 : b.brightness,
                    l = void 0 === b.contrast ? 1 : b.contrast,
                    m = void 0 === b.alpha ? 1 : b.alpha,
                    n = b.destination || [],
                    o = c.colors,
                    p = o.length,
                    q = i - h,
                    r = (q + q / p) / p,
                    s = j ? 255 : 1,
                    t = [];
                for (m *= s, d = 0, e = a.length; e > d; d++) g = a[d], f = h >= g ? 0 : a[d] > i ? p - 1 : Math.floor((g - h) / r), t = o[f] || [0, 0, 0], n[4 * d + 0] = s * (t[0] * l + k), n[4 * d + 1] = s * (t[1] * l + k), n[4 * d + 2] = s * (t[2] * l + k), n[4 * d + 3] = m;
                return n
            }
        };
        return function () {
            if (a) {
                a = a.replace(/^\s+/, "").replace(/\s+$/, "");
                var b, d, e, f, g, h = a.split(/\n/),
                    i = [];
                for (b = 0, e = h.length; e > b; b++)
                    if (g = h[b].replace(/^\s+/, "").replace(/\s+$/, "").split(/\s+/).slice(0, 4), f = g.length, !(3 > f)) {
                        for (d = 0; f > d; d++) g[d] = parseFloat(g[d]);
                        4 > f && g.push(1), i.push(g)
                    }
                c.colors = i
            }
        }(), c
    }
}(),
function () {
    "use strict";
    var a = window.BrainBrowser = window.BrainBrowser || {}, b = "1.5.2";
    a.version = b.indexOf("BRAINBROWSER_VERSION") > 0 ? "D.E.V" : b, a.utils = {
        canvasEnabled: function () {
            return document.createElement("canvas")
        },
        webglEnabled: function () {
            try {
                return !!window.WebGLRenderingContext && !! document.createElement("canvas").getContext("experimental-webgl")
            } catch (a) {
                return !1
            }
        },
        webWorkersEnabled: function () {
            return !!window.Worker
        },
        webGLErrorMessage: function () {
            var a, b = 'BrainBrowser requires <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br/>';
            return b += window.WebGLRenderingContext ? "Your browser seems to support it, but it is <br/> disabled or unavailable.<br/>" : "Your browser does not seem to support it.<br/>", b += 'Test your browser\'s WebGL support <a href="http://get.webgl.org/">here</a>.', a = document.createElement("div"), a.id = "webgl-error", a.innerHTML = b, a
        },
        isFunction: function (a) {
            return a instanceof Function || "function" == typeof a
        },
        isNumeric: function (a) {
            return !isNaN(parseFloat(a))
        },
        eventModel: function (a) {
            var b = [];
            a.addEventListener = function (a, c) {
                b[a] || (b[a] = []), b[a].push(c)
            }, a.triggerEvent = function (c) {
                var d = Array.prototype.slice.call(arguments, 1);
                b[c] && b[c].forEach(function (b) {
                    setTimeout(function () {
                        b.apply(a, d)
                    }, 0)
                })
            }
        },
        min: function () {
            var a = Array.prototype.slice.call(arguments);
            a = 1 === a.length && Array.isArray(a[0]) ? a[0] : a;
            var b, c, d = a[0];
            for (b = 1, c = a.length; c > b; b++) a[b] < d && (d = a[b]);
            return d
        },
        max: function () {
            var a = Array.prototype.slice.call(arguments);
            a = 1 === a.length && Array.isArray(a[0]) ? a[0] : a;
            var b, c, d = a[0];
            for (b = 1, c = a.length; c > b; b++) a[b] > d && (d = a[b]);
            return d
        },
        getOffset: function (a) {
            for (var b = 0, c = 0; a.offsetParent;) b += a.offsetTop, c += a.offsetLeft, a = a.offsetParent;
            return {
                top: b,
                left: c
            }
        },
        checkConfig: function (b) {
            if (!a.config) return !1;
            b = b || "";
            var c, d, e, f = a.config,
                g = b.split(".");
            for (d = 0, e = g.length; e > d; d++) {
                if (c = g[d], !f[c]) return !1;
                f = f[c]
            }
            return !0
        },
        drawDot: function (a, b, c, d) {
            var e = new THREE.SphereGeometry(2),
                f = new THREE.MeshBasicMaterial({
                    color: 16711680
                }),
                g = new THREE.Mesh(e, f);
            g.position.set(b, c, d), a.add(g)
        }
    }, window.requestAnimationFrame = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame || function (a) {
        return window.setTimeout(a, 1e3 / 60)
    }, window.cancelAnimationFrame = window.cancelAnimationFrame || function (a) {
        window.clearTimeout(a)
    }
}(),
function () {
    "use strict";
    var a = window.BrainBrowser = window.BrainBrowser || {}, b = a.VolumeViewer = {};
    b.volumes = {}, b.color_maps = [], b.modules = {}, b.start = function (c, d) {
        function e(a, c) {
            var d = b.volumes[a.type];
            if (!d) throw new Error("Unsuported Volume Type");
            d(a, c)
        }

        function f(a, b, c) {
            var d = c.x,
                e = c.y,
                f = m[b],
                g = n.cachedSlices[a][f] || {
                    width_space: d,
                    height_space: e
                }, h = n.displays[a][f];
            g.image = c.getImage(h.zoom), n.cachedSlices[a][f] = g, h.slice = g, h.updateCursor()
        }

        function g(a) {
            a = a || {}, n.globalUIControls && (n.globalUIControls.defer_until_page_load ? n.addEventListener("ready", function () {
                n.globalUIControls(k)
            }) : n.globalUIControls(k)), h(), l.forEach(function (b, c) {
                var d, e = document.createElement("div"),
                    g = [];
                for (e.classList.add("volume-container"), k.appendChild(e), n.displays.push(j(e, l[c], c, a)), n.cachedSlices[c] = [], b.position.xspace = Math.floor(b.header.xspace.space_length / 2), b.position.yspace = Math.floor(b.header.yspace.space_length / 2), b.position.zspace = Math.floor(b.header.zspace.space_length / 2), g.push(b.slice("xspace", b.position.xspace)), g.push(b.slice("yspace", b.position.yspace)), g.push(b.slice("zspace", b.position.zspace)), d = 0; 3 > d; d++) g[d].vol_id = c, g[d].axis_number = d, g[d].min = b.min, g[d].max = b.max;
                ["xspace", "yspace", "zspace"].forEach(function (a, b) {
                    f(c, a, g[b])
                })
            }), n.triggerEvent("ready"), n.triggerEvent("sliceupdate"),
            function b() {
                window.requestAnimationFrame(b), n.draw()
            }()
        }

        function h() {
            document.addEventListener("keydown", function (a) {
                if (n.active_canvas) {
                    var b = n.active_canvas,
                        c = a.which;
                    if (!(37 > c || c > 40)) {
                        a.preventDefault(), a.stopPropagation();
                        var d = n.active_cursor,
                            e = b.getAttribute("data-volume-id"),
                            f = b.getAttribute("data-axis-name");
                        return {
                            37: function () {
                                d.x--
                            },
                            38: function () {
                                d.y--
                            },
                            39: function () {
                                d.x++
                            },
                            40: function () {
                                d.y++
                            }
                        }[c](), n.setCursor(e, f, d), n.synced && n.displays.forEach(function (a, b) {
                            b !== e && n.setCursor(b, f, d)
                        }), !1
                    }
                }
            }, !1)
        }

        function i(a, b, c, d) {
            var e = document.getElementById(c).innerHTML.replace(/\{\{VOLID\}\}/gm, b),
                f = document.createElement("div");
            f.innerHTML = e;
            var g, h, i, j = f.childNodes,
                k = f.getElementsByClassName(d)[0];
            for (g = 0, h = a.childNodes.length; h > g; g++) i = a.childNodes[g], 1 === i.nodeType && (k.appendChild(i), g--, h--);
            return j
        }

        function j(c, d, e, f) {
            function g(b) {
                var c = {
                    x: 0,
                    y: 0
                };
                return b.addEventListener("mousemove", function (d) {
                    var e, f, g = a.utils.getOffset(b);
                    void 0 !== d.pageX ? (e = d.pageX, f = d.pageY) : (e = d.clientX + document.body.scrollLeft + document.documentElement.scrollLeft, f = d.clientY + document.body.scrollTop + document.documentElement.scrollTop), c.x = e - g.left, c.y = f - g.top
                }, !1), c
            }
            f = f || {};
            var h, j = f.volumes || [],
                k = j[e] || {}, l = [],
                m = k.template || {};
            if (["xspace", "yspace", "zspace"].forEach(function (a) {
                var h = document.createElement("canvas"),
                    i = h.getContext("2d");
                h.width = f.panel_width || 256, h.height = f.panel_height || 256, h.setAttribute("data-volume-id", e), h.setAttribute("data-axis-name", a), h.classList.add("slice-display"), h.style.backgroundColor = "#000000", c.appendChild(h), i.clearRect(0, 0, h.width, h.height), l.push(b.display({
                    volume: d,
                    axis: a,
                    canvas: h,
                    context: i,
                    cursor: {
                        x: h.width / 2,
                        y: h.height / 2
                    },
                    image_center: {
                        x: h.width / 2,
                        y: h.height / 2
                    },
                    mouse: g(h),
                    zoom: n.default_zoom_level
                }))
            }), m.element_id && m.viewer_insert_class && (h = i(c, e, m.element_id, m.viewer_insert_class), "function" == typeof m.complete && m.complete(d, h), Array.prototype.forEach.call(h, function (a) {
                1 === a.nodeType && c.appendChild(a)
            })), n.volumeUIControls) {
                var o = document.createElement("div");
                o.className = "volume-viewer-controls volume-controls", n.volumeUIControls.defer_until_page_load ? n.addEventListener("ready", function () {
                    c.appendChild(o), n.volumeUIControls(o, d, e)
                }) : (n.volumeUIControls(o, d, e), c.appendChild(o))
            }
            return function () {
                var a = null;
                ["xspace", "yspace", "zspace"].forEach(function (b, c) {
                    function d(d) {
                        function f(a) {
                            var b, c;
                            b = g.x - a.last_cursor.x, c = g.y - a.last_cursor.y, a.image_center.x += b, a.image_center.y += c, a.cursor.x += b, a.cursor.y += c, a.last_cursor.x = g.x, a.last_cursor.y = g.y
                        }
                        var g = {
                            x: j.x,
                            y: j.y
                        };
                        d.target === a && (d.shiftKey ? (f(h), n.synced && n.displays.forEach(function (a, b) {
                            b !== e && f(a[c])
                        })) : (n.setCursor(e, b, g), n.synced && n.displays.forEach(function (a, c) {
                            c !== e && n.setCursor(c, b, g)
                        }), h.cursor = n.active_cursor = g))
                    }

                    function f() {
                        document.removeEventListener("mousemove", d, !1), document.removeEventListener("mouseup", f, !1), a = null
                    }

                    function g(a) {
                        var b = Math.max(-1, Math.min(1, a.wheelDelta || -a.detail));
                        a.preventDefault(), a.stopPropagation(), h.zoom = Math.max(h.zoom + .05 * b, .05), n.fetchSlice(e, ["xspace", "yspace", "zspace"][c]), n.synced && n.displays.forEach(function (a, d) {
                            if (d !== e) {
                                var f = a[c];
                                f.zoom = Math.max(f.zoom + .05 * b, .05), n.fetchSlice(d, ["xspace", "yspace", "zspace"][c])
                            }
                        })
                    }
                    var h = l[c],
                        i = h.canvas,
                        j = h.mouse;
                    i.addEventListener("mousedown", function (g) {
                        a = g.target;
                        var i = {
                            x: j.x,
                            y: j.y
                        };
                        g.preventDefault(), g.stopPropagation(), g.shiftKey ? (h.last_cursor.x = i.x, h.last_cursor.y = i.y, n.synced && n.displays.forEach(function (a, b) {
                            if (b !== e) {
                                var d = a[c];
                                d.last_cursor.x = i.x, d.last_cursor.y = i.y
                            }
                        })) : (n.setCursor(e, b, i), n.synced && n.displays.forEach(function (a, c) {
                            c !== e && n.setCursor(c, b, i)
                        }), h.cursor = n.active_cursor = i), n.active_canvas = g.target, document.addEventListener("mousemove", d, !1), document.addEventListener("mouseup", f, !1)
                    }, !1), i.addEventListener("mousewheel", g, !1), i.addEventListener("DOMMouseScroll", g, !1)
                })
            }(), l
        }
        var k, l = [],
            m = {
                xspace: 0,
                yspace: 1,
                zspace: 2
            }, n = {
                volumes: l,
                displays: [],
                synced: !1,
                default_zoom_level: 1,
                cachedSlices: []
            };
        a.utils.eventModel(n), Object.keys(b.modules).forEach(function (a) {
            b.modules[a](n)
        }), console.log("BrainBrowser Volume Viewer v" + a.version), n.loadVolumes = function (d) {
            if (!a.utils.checkConfig("volume_viewer.color_maps")) throw new Error("error in VolumeViewer configuration.\nBrainBrowser.config.volume_viewer.color_maps not defined.");
            d = d || {};
            var f = d.overlay && "object" == typeof d.overlay ? d.overlay : {};
            k = document.getElementById(c);
            var h = d.volumes,
                i = d.volumes.length,
                j = a.config.volume_viewer,
                m = j.color_maps[0];
            b.loader.loadColorScaleFromURL(m.url, m.name, function (a) {
                function c(a) {
                    e(h[a], function (b) {
                        b.position = {}, l[a] = b, ++k < i || (d.overlay && i > 1 ? (d.volumes.push(f), e({
                            volumes: n.volumes,
                            type: "overlay"
                        }, function (a) {
                            a.position = {}, l.push(a), g(d)
                        })) : g(d))
                    })
                }
                var j, k = 0;
                for (a.cursor_color = m.cursor_color, n.default_color_map = a, b.color_maps[0] = a, j = 0; i > j; j++) c(j)
            }), j.color_maps.slice(1).forEach(function (a, c) {
                b.loader.loadColorScaleFromURL(a.url, a.name, function (d) {
                    d.cursor_color = a.cursor_color, b.color_maps[c + 1] = d
                })
            })
        }, n.draw = function () {
            var a, b, c, d, e = 4,
                f = e / 2;
            l.forEach(function (g, h) {
                n.displays[h].forEach(function (i, j) {
                    c = i.canvas, b = i.context, g = l[h], b.globalAlpha = 255, b.clearRect(0, 0, c.width, c.height), a = n.cachedSlices[h][j], a && (d = g.color_map || n.default_color_map, i.drawSlice(), i.drawCursor(d.cursor_color)), c === n.active_canvas && (b.save(), b.strokeStyle = "#EC2121", b.lineWidth = e, b.strokeRect(f, f, c.width - e, c.height - e), b.restore())
                })
            })
        };
        var o = {};
        n.fetchSlice = function (a, b, c) {
            o[a] = o[a] || {}, clearTimeout(o[a][b]), o[a][b] = setTimeout(function () {
                var d, e = n.volumes[a],
                    g = m[b];
                void 0 === c && (c = e.position[b]), d = e.slice(b, c, e.current_time), d.vol_id = a, d.axis_number = g, e.position[b] = c, d.min = e.min, d.max = e.max, f(a, b, d), n.triggerEvent("sliceupdate")
            }, 0)
        }, n.setCursor = function (a, b, c) {
            var d, e, f = m[b],
                g = n.cachedSlices[a][f],
                h = n.displays[a][f],
                i = h.getImageOrigin(),
                j = h.zoom;
            h.cursor.x = c.x, h.cursor.y = c.y, c ? (d = Math.floor((c.x - i.x) / j / Math.abs(g.width_space.step)), e = Math.floor(g.height_space.space_length - (c.y - i.y) / j / Math.abs(g.height_space.step))) : (d = null, e = null), n.fetchSlice(a, g.width_space.name, d), n.fetchSlice(a, g.height_space.name, e)
        }, n.redrawVolumes = function () {
            n.volumes.forEach(function (a, b) {
                n.fetchSlice(b, "xspace", a.position.xspace), n.fetchSlice(b, "yspace", a.position.yspace), n.fetchSlice(b, "zspace", a.position.zspace)
            })
        }, d(n)
    }
}(),
function () {
    "use strict";
    var a = {
        updateCursor: function () {
            var a = this.volume,
                b = this.slice,
                c = this.getImageOrigin();
            a && b && (this.cursor.x = a.position[b.width_space.name] * Math.abs(b.width_space.step) * this.zoom + c.x, this.cursor.y = (b.height_space.space_length - a.position[b.height_space.name]) * Math.abs(b.height_space.step) * this.zoom + c.y)
        },
        getImageOrigin: function () {
            return {
                x: this.image_center.x - this.slice.image.width / 2,
                y: this.image_center.y - this.slice.image.height / 2
            }
        },
        drawSlice: function () {
            var a = this.slice.image,
                b = this.getImageOrigin();
            this.context.putImageData(a, b.x, b.y)
        },
        drawCursor: function (a) {
            var b = this.context,
                c = this.zoom,
                d = 8;
            a = a || "#FF0000", b.save(), b.strokeStyle = a, b.translate(this.cursor.x, this.cursor.y), b.scale(c, c), b.lineWidth = 2, b.beginPath(), b.moveTo(0, -d), b.lineTo(0, -2), b.moveTo(0, 2), b.lineTo(0, d), b.moveTo(-d, 0), b.lineTo(-2, 0), b.moveTo(2, 0), b.lineTo(d, 0), b.stroke(), b.restore()
        }
    };
    BrainBrowser.VolumeViewer.display = function (b) {
        b = b || {};
        var c = {
            cursor: {
                x: 0,
                y: 0
            },
            last_cursor: {
                x: 0,
                y: 0
            },
            image_center: {
                x: 0,
                y: 0
            },
            zoom: 1
        }, d = Object.create(a);
        return Object.keys(c).forEach(function (a) {
            d[a] = c[a]
        }), Object.keys(b).forEach(function (a) {
            d[a] = b[a]
        }), d
    }
}(),
function () {
    "use strict";
    var a = BrainBrowser.VolumeViewer;
    a.loader = {
        loadArrayBuffer: function (a, b) {
            var c, d = new XMLHttpRequest;
            d.open("GET", a), d.responseType = "arraybuffer", d.onreadystatechange = function () {
                if (4 === d.readyState) {
                    if (c = d.status, !(c >= 200 && 300 > c || 304 === c)) throw new Error("error loading URL: " + a + "\n" + "HTTP Response: " + d.status + "\n" + "HTTP Status: " + d.statusText + "\n" + "Response was: \n" + d.response);
                    b(d.response)
                }
            }, d.send()
        },
        loadFromTextFile: function (a, b) {
            var c = new FileReader,
                d = a.files;
            c.file = d[0], c.onloadend = function (a) {
                b(a.target.result)
            }, c.readAsText(d[0])
        },
        loadFromURL: function (a, b, c) {
            $.ajax({
                url: a,
                type: "GET",
                success: b,
                error: c
            })
        },
        loadColorScaleFromFile: function (b, c, d) {
            a.loader.loadFromTextFile(b, function (a) {
                var b = BrainBrowser.createColorMap(a);
                b.name = c, d(b)
            })
        },
        loadColorScaleFromURL: function (b, c, d) {
            a.loader.loadFromURL(b, function (a) {
                var b = BrainBrowser.createColorMap(a);
                b.name = c, d(b)
            })
        }
    }
}(), BrainBrowser.VolumeViewer.utils = function () {
    "use strict";

    function a(a, b, c, d, e, f) {
        var g, h, i, j, k, l = [];
        for (f = f || 1, g = 0; b > g; g++)
            for (h = 0; c > h; h++)
                for (j = d ? b - g - 1 : g, k = e ? c - h - 1 : h, i = 0; f > i; i++) l[(h * b + g) * f + i] = a[(k * b + j) * f + i];
        return l
    }

    function b(b, c, d) {
        var e = b.data,
            f = b.width,
            g = b.height,
            h = document.createElement("canvas").getContext("2d"),
            i = 4;
        if (f === c && g === d) return b;
        0 > c && d > 0 && (e = a(e, f, g, !0, !1, i)), c = Math.abs(c), d = Math.abs(d);
        for (var j = h.createImageData(c, d), k = j.data, l = f / c, m = g / d, n = 0; d > n; n++)
            for (var o = 0; c > o; o++)
                for (var p = Math.floor(o * l), q = Math.floor(n * m), r = 0; i > r; r++) k[Math.floor(n * c + o) * i + r] = e[Math.floor(q * f + p) * i + r];
        return j
    }
    return {
        nearestNeighbor: b
    }
}(),
function () {
    "use strict";

    function a(a, b) {
        var c, d, e = new XMLHttpRequest;
        e.open("GET", a), e.onreadystatechange = function () {
            if (4 === e.readyState) {
                if (c = e.status, !(c >= 200 && 300 > c || 304 === c)) throw new Error("error loading URL: " + a + "\n" + "HTTP Response: " + e.status + "\n" + "HTTP Status: " + e.statusText + "\n" + "Response was: \n" + e.response);
                try {
                    d = JSON.parse(e.response)
                } catch (f) {
                    throw new Error("server did not respond with valid JSON\nResponse was: \n" + e.response)
                }
                b && b(d)
            }
        }, e.send()
    }

    function b(a, b) {
        c.loader.loadArrayBuffer(a, function (a) {
            b(a)
        })
    }
    var c = BrainBrowser.VolumeViewer,
        d = {
            slice: function (a, b, d) {
                var e = this.data.slice(a, b, d);
                return e.color_map = this.color_map || c.color_maps[0], e.min = this.min, e.max = this.max, e.axis = a, e.getImage = function (a) {
                    a = a || 1;
                    var b = document.createElement("canvas").getContext("2d"),
                        d = this.color_map,
                        e = b.createImageData(this.width, this.height);
                    d.mapColors(this.data, {
                        min: this.min,
                        max: this.max,
                        scale255: !0,
                        brightness: 0,
                        contrast: 1,
                        alpha: this.alpha,
                        destination: e.data
                    });
                    var f = this.x.step,
                        g = this.y.step;
                    return c.utils.nearestNeighbor(e, Math.floor(this.width * f * a), Math.floor(this.height * g * a))
                }, e
            },
            getVoxelCoords: function () {
                return {
                    x: this.position.xspace,
                    y: this.position.yspace,
                    z: this.position.zspace
                }
            },
            setVoxelCoords: function (a, b, c) {
                this.position.xspace = a, this.position.yspace = b, this.position.zspace = c
            },
            getWorldCoords: function () {
                return {
                    x: this.data.xspace.start + this.position.xspace * this.data.xspace.step,
                    y: this.data.yspace.start + this.position.yspace * this.data.yspace.step,
                    z: this.data.zspace.start + this.position.zspace * this.data.zspace.step
                }
            },
            setWorldCoords: function (a, b, c) {
                this.position.xspace = Math.floor((a - this.data.xspace.start) / this.data.xspace.step), this.position.yspace = Math.floor((b - this.data.yspace.start) / this.data.yspace.step), this.position.zspace = Math.floor((c - this.data.zspace.start) / this.data.zspace.step)
            }
        };
    c.volumes.minc = function (e, f) {
        var g, h = Object.create(d);
        h.current_time = 0, a(e.header_url, function (a) {
            b(e.raw_data_url, function (b) {
                g = new Uint8Array(b), h.data = c.mincData(e.filename, a, g), h.header = h.data.header, h.min = 0, h.max = 255, f && f(h)
            })
        })
    }
}(), BrainBrowser.VolumeViewer.mincData = function () {
    "use strict";

    function a(a, b, c) {
        for (var d = new Uint16Array(b * c), e = 0; b > e; e++)
            for (var f = 0; c > f; f++) d[e * c + f] = a[f * b + (b - e)];
        return d
    }

    function b(a, b, c) {
        for (var d = new Uint16Array(b * c), e = 0; b > e; e++)
            for (var f = 0; c > f; f++) d[e * c + f] = a[(c - f) * b + e];
        return d
    }
    var c = {
        parseHeader: function (a) {
            this.header = a, this.order = a.order, 4 === this.order.length && (this.order = this.order.slice(1), this.time = a.time), this.xspace = a.xspace, this.yspace = a.yspace, this.zspace = a.zspace, this.xspace.name = "xspace", this.yspace.name = "yspace", this.zspace.name = "zspace", this.xspace.space_length = parseFloat(this.xspace.space_length), this.yspace.space_length = parseFloat(this.yspace.space_length), this.zspace.space_length = parseFloat(this.zspace.space_length), this.xspace.start = parseFloat(this.xspace.start), this.yspace.start = parseFloat(this.yspace.start), this.zspace.start = parseFloat(this.zspace.start), this.xspace.step = parseFloat(this.xspace.step), this.yspace.step = parseFloat(this.yspace.step), this.zspace.step = parseFloat(this.zspace.step), 4 === this.order.length && (this.time.space_length = parseFloat(this.time.space_length), this.time.start = parseFloat(this.time.start), this.time.step = parseFloat(this.time.step));
            var b = this[this.order[0]],
                c = this[this.order[1]],
                d = this[this.order[2]];
            b.height = parseFloat(c.space_length), b.height_space = c, b.width = parseFloat(d.space_length), b.width_space = d, c.height = parseFloat(d.space_length), c.height_space = d, c.width = parseFloat(b.space_length), c.width_space = b, d.height = parseFloat(c.space_length), d.height_space = c, d.width = parseFloat(b.space_length), d.width_space = b, b.offset = parseFloat(c.space_length) * parseFloat(d.space_length), c.offset = parseFloat(b.space_length), d.offset = parseFloat(b.space_length), b.slice_length = b.height * b.width
        },
        slice: function (c, d, e) {
            var f, g = this.cachedSlices,
                h = this[this.order[0]];
            if (e = e || 0, g[c] = g[c] || [], g[c][e] = g[c][e] || [], this[c].step < 0 && (d = this[c].space_length - d), void 0 !== g[c][e][d]) return f = g[c][e][d], f.alpha = 1, f.number = d, f;
            if (void 0 === this.order) return !1;
            var i = 0;
            this.time && (i = e * h.height * h.width * parseFloat(h.space_length));
            var j = this[c].width_space.step,
                k = this[c].height_space.step;
            f = {};
            var l, m, n, o, p, q, r, s, t, u;
            if (this.order[0] === c)
                if (m = this[c].height * this[c].width, n = this[c].height, o = this[c].width, p = 1, q = o, r = m, l = new Uint16Array(m), j > 0)
                    if (k > 0)
                        for (s = 0; m > s; s++) l[s] = this.data[i + r * d + s];
                    else
                        for (s = n; s > 0; s--)
                            for (t = 0; o > t; t++) l[(n - s) * o + t] = this.data[i + r * d + s * o + t];
                    else if (0 > k)
                for (s = 0; n > s; s++)
                    for (t = 0; o > t; t++) l[s * o + t] = this.data[i + r * d + s * o + o - t];
            else
                for (s = n; s > 0; s--)
                    for (t = 0; o > t; t++) l[(n - s) * o + t] = this.data[i + r * d + s * o + o - t];
            else if (this.order[1] === c)
                if (n = this[c].height, m = this[c].height * this[c].width, o = this[c].width, p = 1, q = h.slice_length, r = h.width, l = new Uint8Array(m), 0 > k)
                    for (t = 0; n > t; t++)
                        for (u = 0; o > u; u++) l[t * o + u] = this.data[i + d * r + q * u + t];
                else
                    for (t = n; t >= 0; t--)
                        for (u = 0; o > u; u++) l[(n - t) * o + u] = this.data[i + d * r + q * u + t];
                else
                    for (n = this[c].height, m = this[c].height * this[c].width, o = this[c].width, p = h.slice_length, q = h.width, r = 1, l = new Uint16Array(m), t = 0; n > t; t++)
                        for (u = 0; o > u; u++) l[t * o + u] = this.data[i + d + h.width * t + u * h.slice_length];
            return f.x = this[c].width_space, f.y = this[c].height_space, f.width = o, f.height = n, "xspace" === c && "yspace" === this.xspace.height_space.name && (l = this.zspace.step < 0 ? b(l, f.width, f.height) : a(l, f.width, f.height), f.x = this[c].height_space, f.y = this[c].width_space, f.width = n, f.height = o), "yspace" === c && "xspace" === this.yspace.height_space.name && (l = this.zspace.step < 0 ? b(l, f.width, f.height) : a(l, f.width, f.height), f.x = this[c].height_space, f.y = this[c].width_space, f.width = n, f.height = o), "zspace" === c && "xspace" === this.zspace.height_space.name && (l = a(l, f.width, f.height), f.x = this[c].width_space, f.y = this[c].height_space, f.width = n, f.height = o), f.data = l, f.x = f.x || this[c].width_space, f.y = f.y || this[c].height_space, f.width = f.width || o, f.height = f.height || n, g[c][e][d] = f, f
        }
    };
    return function (a, b, d) {
        var e = Object.create(c);
        return e.cachedSlices = {}, e.parseHeader(b), e.data = d, e
    }
}(),
function () {
    "use strict";

    function a(a, b) {
        var c, d, e, f, g, h, i, j = a.length,
            k = b.data,
            l = b.width,
            m = b.height,
            n = [];
        if (j > 1) {
            for (c = 0; m > c; c += 1)
                for (e = 0; l > e; e += 1)
                    for (d = 0; j > d; d += 1) n[d] = n[d] || 0, f = a[d], c < f.height && e < f.width && (g = 4 * (c * l + e), h = (k[g + 3] || 0) / 255, i = n[d], k[g] = (k[g + 0] || 0) * h + f.data[i + 0] * (f.data[i + 3] / 255), k[g + 1] = (k[g + 1] || 0) * h + f.data[i + 1] * (f.data[i + 3] / 255), k[g + 2] = (k[g + 2] || 0) * h + f.data[i + 2] * (f.data[i + 3] / 255), k[g + 3] = (k[g + 3] || 0) + f.data[i + 3], n[d] += 4);
            for (c = 3; c < k.length; c += 4) k[c] = 255;
            return b
        }
        return a[0]
    }

    function b(a) {
        var b = Object.create(d);
        return b.volumes = [], b.blend_ratios = [], a.forEach(function (c) {
            b.volumes.push(c), b.blend_ratios.push(1 / a.length)
        }), b
    }
    var c = BrainBrowser.VolumeViewer,
        d = {
            slice: function (b, d, e) {
                var f = this,
                    g = [];
                return f.volumes.forEach(function (a, c) {
                    var h = a.slice(b, d, e);
                    h.alpha = f.blend_ratios[c], g.push(h)
                }), {
                    x: g[0].x,
                    y: g[0].y,
                    getImage: function (b) {
                        b = b || 1;
                        var d, e, f, h = document.createElement("canvas").getContext("2d"),
                            i = [];
                        return g.forEach(function (a) {
                            var d = a.color_map,
                                e = h.createImageData(a.width, a.height),
                                f = Math.floor(a.width * a.x.step * b),
                                g = Math.floor(a.height * a.y.step * b);
                            d.mapColors(a.data, {
                                min: a.min,
                                max: a.max,
                                scale255: !0,
                                brightness: 0,
                                contrast: 1,
                                alpha: a.alpha,
                                destination: e.data
                            }), e = c.utils.nearestNeighbor(e, f, g), i.push(e)
                        }), d = i.reduce(function (a, b) {
                            return Math.max(a, b.width)
                        }, 0), e = i.reduce(function (a, b) {
                            return Math.max(a, b.height)
                        }, 0), f = h.createImageData(d, e), a(i, f)
                    }
                }
            },
            getVoxelCoords: function () {
                return {
                    x: this.position.xspace,
                    y: this.position.yspace,
                    z: this.position.zspace
                }
            },
            setVoxelCoords: function (a, b, c) {
                this.position.xspace = a, this.position.yspace = b, this.position.zspace = c
            },
            getWorldCoords: function () {
                var a = this.volumes[0];
                return {
                    x: a.data.xspace.start + this.position.xspace * a.data.xspace.step,
                    y: a.data.yspace.start + this.position.yspace * a.data.yspace.step,
                    z: a.data.zspace.start + this.position.zspace * a.data.zspace.step
                }
            },
            setWorldCoords: function (a, b, c) {
                var d = this.volumes[0];
                this.position.xspace = Math.floor((a - d.data.xspace.start) / d.data.xspace.step), this.position.yspace = Math.floor((b - d.data.yspace.start) / d.data.yspace.step), this.position.zspace = Math.floor((c - d.data.zspace.start) / d.data.zspace.step)
            }
        };
    c.volumes.overlay = function (a, c) {
        var d = b(a.volumes);
        d.type = "overlay", d.min = 0, d.max = 255, d.header = a.volumes[0].header, setTimeout(function () {
            c(d)
        }, 0)
    }
}();
