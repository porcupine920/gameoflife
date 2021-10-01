(ns gameoflife.core
  (:require [reagent.dom :as rdom]
            [reagent.core :as reagent]))

(enable-console-print!)

(def window-width (reagent/atom nil))
;;(.setTransform ctx 1, 0, 0, 1, 0.5, 0.5)

(def glider-gun #{[0 24] [1 22] [1 24] [2 12] [2 13] [2 20] [2 21] [2 34] [2 35] [3 11] [3 15] [3 20] [3 21] [3 34] [3 35] [4 0] [4 1] [4 10] [4 16] [4 20] [4 21] [5 0] [5 1] [5 10] [5 14] [5 16] [5 17] [5 22] [5 24] [6 10] [6 16] [6 24] [7 11] [7 15] [8 12] [8 13]})

(def glider-duplicator #{[0 7] [0 12] [0 13] [0 37] [0 38] [1 8] [1 13] [1 37] [1 38] [2 6] [2 7] [2 8] [2 13] [2 15] [2 23] [2 34] [2 35] [2 42] [2 46] [2 47] [3 14] [3 15] [3 23] [3 25] [3 33] [3 34] [3 35] [3 41] [3 45] [3 47] [4 26] [4 27] [4 34] [4 35] [4 42] [4 43] [4 44] [4 45] [4 46] [5 26] [5 27] [5 37] [5 38] [5 43] [5 44] [5 45] [6 26] [6 27] [6 37] [6 38] [7 23] [7 25] [8 23] [11 24] [11 25] [12 24] [12 25] [16 25] [16 26] [16 27] [17 27] [18 26] [21 9] [21 11] [22 7] [22 11] [22 17] [22 18] [22 19] [23 0] [23 1] [23 7] [23 15] [23 17] [23 20] [23 23] [23 24] [24 0] [24 1] [24 6] [24 11] [24 19] [24 20] [24 23] [24 26] [25 7] [25 27] [26 7] [26 11] [26 14] [26 15] [26 16] [26 27] [27 9] [27 11] [27 27] [28 23] [28 26] [28 32] [28 33] [29 23] [29 24] [29 32] [29 34] [30 34] [31 34] [31 35] })

(def initial glider-gun)

(def cells (reagent/atom initial))

(def mouse-down? (reagent/atom false))

;; define your app data so that it doesn't get over-written on reload

(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)] [(+ dx x) (+ dy y)]))

(defn step
  [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))] loc)))

(defn draw-background [ctx w h]
  (.beginPath ctx)
  (.rect ctx 0 0 w h)
  (set! (.-fillStyle ctx) "black")
  (.fill ctx)
  (.stroke ctx))

(def radius 10)

(defn draw-cell [ctx x y]
  (.beginPath ctx)
  (.arc ctx (+ y radius) (+ x radius) radius 0 (* 2 js/Math.PI) false)
  (set! (.-fillStyle ctx) "lightgreen")
  (.fill ctx)
  (.stroke ctx))

(def scale 20)

(def xoffset 2)

(def yoffset 0)

(set! yoffset (quot (- (quot (.-innerWidth js/window) scale) (reduce max (map second initial))) 2))

(defn transform [loc]
  (* loc scale))

(defn draw-cells [ctx cells]
  (doseq [[x y] cells]
    (draw-cell ctx (transform (+ x xoffset)) (transform (+ y yoffset)))))

(defn draw-canvas-contents [canvas]
  (let [ctx (.getContext canvas "2d")
        w (.-clientWidth canvas)
        h (.-clientHeight canvas)]
    (draw-background ctx w h)
    (draw-cells ctx @cells)))

(defn div-with-canvas [ ]
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn [ this ]
        (draw-canvas-contents (.-firstChild @dom-node)))

      :component-did-mount
      (fn [ this ]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn [ ]
        @window-width
        [:div.with-canvas
         [:canvas (if-let [ node @dom-node ]
                    {:width (.-clientWidth node)
                     :height (.-clientHeight node)})]])})))

(defn home []
  [div-with-canvas])

(js/setInterval (fn []
                  (swap! cells #(step %))
                  (rdom/render [home] (.getElementById js/document "app"))) 300)

(defn on-window-resize [ evt ]
  (reset! window-width (.-innerWidth js/window))
  (set! yoffset (quot (- (quot (.-innerWidth js/window) scale) (reduce max (map second initial))) 2)))

(defn on-mouse-down [evt]
  (reset! mouse-down? true)
  (swap! cells #(conj % %2) [(- (quot (.-offsetY evt) scale) xoffset) (- (quot (.-offsetX evt) scale) yoffset)])
  (rdom/render [home] (.getElementById js/document "app")))

(defn on-mouse-move [evt]
  (when @mouse-down?
    (swap! cells #(conj % %2) [(- (quot (.-offsetY evt) scale) xoffset) (- (quot (.-offsetX evt) scale) yoffset)])
    (rdom/render [home] (.getElementById js/document "app"))))

(defn on-mouse-up [evt]
  (reset! mouse-down? false))

(defn ^:export main []
  (rdom/render [home]
               (.getElementById js/document "app"))
  (.addEventListener js/window "resize" on-window-resize)
  (.addEventListener js/window "mousedown" on-mouse-down)
  (.addEventListener js/window "mousemove" on-mouse-move)
  (.addEventListener js/window "mouseup" on-mouse-up))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
