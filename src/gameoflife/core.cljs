(ns gameoflife.core
  (:require [reagent.dom :as rdom]
            [reagent.core :as reagent]))

(enable-console-print!)

(def window-width (reagent/atom nil))
;;(.setTransform ctx 1, 0, 0, 1, 0.5, 0.5)

(def glider-gun #{[0 24] [1 22] [1 24] [2 12] [2 13] [2 20] [2 21] [2 34] [2 35] [3 11] [3 15] [3 20] [3 21] [3 34] [3 35] [4 0] [4 1] [4 10] [4 16] [4 20] [4 21] [5 0] [5 1] [5 10] [5 14] [5 16] [5 17] [5 22] [5 24] [6 10] [6 16] [6 24] [7 11] [7 15] [8 12] [8 13]})

(def cells (reagent/atom glider-gun))

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

(def xoffset 0)

(def yoffset 0)

(defn transform [loc]
  (* loc scale))

(defn draw-cells [ctx cells]
  (doseq [[x y] @cells]
    (draw-cell ctx (transform (+ x xoffset)) (transform (+ y yoffset)))))

(defn draw-canvas-contents [ canvas ]
  (let [ctx (.getContext canvas "2d")
        w (.-clientWidth canvas)
        h (.-clientHeight canvas)]
;;    (set! yoffset (quot (- (quot w scale) (reduce max (map second @cells))) 2))
    (draw-background ctx w h)
    (draw-cells ctx cells)))

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
  (set! yoffset (quot (- (quot (.-innerWidth js/window) scale) (reduce max (map second @cells))) 2))
  [div-with-canvas])

(js/setInterval (fn []
                  (swap! cells #(step %))
                  (reagent.dom/render [home] (.getElementById js/document "app"))) 500)

(defn on-window-resize [ evt ]
  (reset! window-width (.-innerWidth js/window))
  (set! yoffset (quot (- (quot (.-innerWidth js/window) scale) (reduce max (map second @cells))) 2)))

(defn ^:export main []
  (rdom/render [home]
               (.getElementById js/document "app"))
  (.addEventListener js/window "resize" on-window-resize))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
